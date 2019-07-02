package org.sunbird.helper;

import akka.util.Timeout;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.sort.SortOrder;
import org.sunbird.dto.SearchDTO;
import org.sunbird.util.LoggerEnum;
import org.sunbird.util.ProjectLogger;
import scala.concurrent.Await;
import scala.concurrent.Future;

/**
 * This class will provide all required operation for elastic search.
 *
 * @author arvind
 * @author Manzarul
 * @author mayank:github.com/iostream04
 */
public class ElasticSearchHelper {

  public static final String LTE = "<=";
  public static final String LT = "<";
  public static final String GTE = ">=";
  public static final String GT = ">";
  public static final String ASC_ORDER = "ASC";
  public static final String STARTS_WITH = "startsWith";
  public static final String ENDS_WITH = "endsWith";
  public static final String SOFT_MODE = "soft";
  public static final String RAW_APPEND = ".raw";
  protected static Map<String, Boolean> indexMap = new HashMap<>();
  protected static Map<String, Boolean> typeMap = new HashMap<>();
  protected static final String ES_CONFIG_FILE = "elasticsearch.conf";
  public static final int WAIT_TIME = 5;     // elastic search time out
  public static Timeout timeout = new Timeout(WAIT_TIME, TimeUnit.SECONDS);
  public static final List<String> upsertResults =
      new ArrayList<>(Arrays.asList("CREATED", "UPDATED", "NOOP"));
  private static final String _DOC = "_doc";

  private ElasticSearchHelper() {}

  /**
   * This method will return the object after getting complete future.
   *
   * @param future
   * @return Object which future inherits
   */
  @SuppressWarnings("unchecked")
  public static Object getResponseFromFuture(Future future) {
    try {
      Object result = Await.result(future, timeout.duration());
      return result;
    } catch (Exception e) {
      ProjectLogger.log(
          "ElasticSearchHelper:getResponseFromFuture: error occured " + e, LoggerEnum.INFO.name());
    }
    return null;
  }

  /**
   * This method adds aggregations to the incoming SearchRequestBuilder object
   *
   * @param searchRequestBuilder which will be updated with facets if any present
   * @param facets Facets provide aggregated data based on a search query
   * @return SearchRequestBuilder
   */
  public static SearchRequestBuilder addAggregations(
      SearchRequestBuilder searchRequestBuilder, List<Map<String, String>> facets) {
    long startTime = System.currentTimeMillis();
    ProjectLogger.log(
        "ElasticSearchHelper:addAggregations: method started at ==" + startTime,
        LoggerEnum.PERFLOG.name());

    if (facets != null && !facets.isEmpty()) {
      Map<String, String> map = facets.get(0);
      if (!MapUtils.isEmpty(map)) {
        for (Map.Entry<String, String> entry : map.entrySet()) {

          String key = entry.getKey();
          String value = entry.getValue();
          if (EsConstant.DATE_HISTOGRAM.equalsIgnoreCase(value)) {
            searchRequestBuilder.addAggregation(
                AggregationBuilders.dateHistogram(key)
                    .field(key + RAW_APPEND)
                    .dateHistogramInterval(DateHistogramInterval.days(1)));

          } else if (null == value) {
            searchRequestBuilder.addAggregation(
                AggregationBuilders.terms(key).field(key + RAW_APPEND));
          }
        }
      }

      ProjectLogger.log(
          "ElasticSearchHelper:addAggregations method end =="
              + " ,Total time elapsed = "
              + calculateEndTime(startTime),
          LoggerEnum.PERFLOG.name());
    }

    return searchRequestBuilder;
  }

  /**
   * This method returns any constraints defined in searchDto object
   *
   * @param searchDTO with constraints
   * @return Map for constraints present in serachDTO
   */
  public static Map<String, Float> getConstraints(SearchDTO searchDTO) {
    if (null != searchDTO.getSoftConstraints() && !searchDTO.getSoftConstraints().isEmpty()) {
      return searchDTO
          .getSoftConstraints()
          .entrySet()
          .stream()
          .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().floatValue()));
    }
    return Collections.emptyMap();
  }

  /**
   * This method return SearchRequestBuilder for transport client
   *
   * @param client transport client instance
   * @param index to be checkout
   * @return SearchRequestBuilder for a provided request
   */
  public static SearchRequestBuilder getTransportSearchBuilder(
      TransportClient client, String[] index) {
    return client.prepareSearch().setIndices(index).setTypes(_DOC);
  }

  /**
   * Method to add the additional search query like range query , exists - not exist filter etc.
   *
   * @param query query which will be updated
   * @param entry which will have key to be search and respective values
   * @param constraintsMap constraints on key and values
   */
  @SuppressWarnings("unchecked")
  public static void addAdditionalProperties(
      BoolQueryBuilder query, Entry<String, Object> entry, Map<String, Float> constraintsMap) {
    long startTime = System.currentTimeMillis();

    ProjectLogger.log(
        "ElasticSearchHelper:addAdditionalProperties: method started at ==" + startTime,
        LoggerEnum.PERFLOG.name());

    String key = entry.getKey();
    if (EsConstant.FILTERS.equalsIgnoreCase(key)) {

      Map<String, Object> filters = (Map<String, Object>) entry.getValue();
      for (Map.Entry<String, Object> en : filters.entrySet()) {
        query = createFilterESOpperation(en, query, constraintsMap);
      }
    } else if (EsConstant.EXISTS.equalsIgnoreCase(key)
        || EsConstant.NOT_EXISTS.equalsIgnoreCase(key)) {
      query = createESOpperation(entry, query, constraintsMap);
    }
    ProjectLogger.log(
        "ElasticSearchHelper:addAdditionalProperties: method end =="
            + " ,Total time elapsed = "
            + calculateEndTime(startTime),
        LoggerEnum.PERFLOG.name());
  }

  /**
   * Method to create CommonTermQuery , multimatch and Range Query.
   *
   * @param entry which contains key for search and respective values
   * @param query Object which will be updated
   * @param constraintsMap constraints for key and values
   * @return BoolQueryBuilder
   */
  @SuppressWarnings("unchecked")
  private static BoolQueryBuilder createFilterESOpperation(
      Entry<String, Object> entry, BoolQueryBuilder query, Map<String, Float> constraintsMap) {
    ProjectLogger.log(
        "ElasticSearchHelper:createFilterESOpperation: method started ", LoggerEnum.INFO.name());

    String key = entry.getKey();
    Object val = entry.getValue();
    if (val instanceof List && val != null) {
      query = getTermQueryFromList(val, key, query, constraintsMap);
    } else if (val instanceof Map) {
      query = getTermQueryFromMap(val, key, query, constraintsMap);
    } else if (val instanceof String) {
      query.must(
          createTermQuery(key + RAW_APPEND, ((String) val).toLowerCase(), constraintsMap.get(key)));
    } else {
      query.must(createTermQuery(key + RAW_APPEND, val, constraintsMap.get(key)));
    }
    ProjectLogger.log(
        "ElasticSearchHelper:createFilterESOpperation: method end ", LoggerEnum.INFO.name());

    return query;
  }

  /**
   * This method returns termQuery if any present in map provided
   *
   * @param key for search in termquery
   * @param val value of the key to be searched
   * @param query which will be updated according to key , value and constraints
   * @param constraintsMap for setting any constraints on values for the specified key
   * @return BoolQueryBuilder
   */
  private static BoolQueryBuilder getTermQueryFromMap(
      Object val, String key, BoolQueryBuilder query, Map<String, Float> constraintsMap) {
    ProjectLogger.log(
        "ElasticSearchHelper:getTermQueryFromMap: method started ", LoggerEnum.INFO.name());

    Map<String, Object> value = (Map<String, Object>) val;
    Map<String, Object> rangeOperation = new HashMap<>();
    Map<String, Object> lexicalOperation = new HashMap<>();
    for (Map.Entry<String, Object> it : value.entrySet()) {
      String operation = it.getKey();
      if (operation.startsWith(LT) || operation.startsWith(GT)) {
        rangeOperation.put(operation, it.getValue());
      } else if (operation.startsWith(STARTS_WITH) || operation.startsWith(ENDS_WITH)) {
        lexicalOperation.put(operation, it.getValue());
      }
    }
    if (!(rangeOperation.isEmpty())) {
      query.must(createRangeQuery(key, rangeOperation, constraintsMap.get(key)));
    }
    if (!(lexicalOperation.isEmpty())) {
      query.must(createLexicalQuery(key, lexicalOperation, constraintsMap.get(key)));
    }
    ProjectLogger.log(
        "ElasticSearchHelper:getTermQueryFromMap: method end ", LoggerEnum.INFO.name());

    return query;
  }

  /**
   * This method returns termQuery if any present in List provided
   *
   * @param key for search in termquery
   * @param val value of the key to be searched
   * @param query which will be updated according to key , value and constraints
   * @param constraintsMap for setting any constraints on values for the specified key
   * @return BoolQueryBuilder
   */
  private static BoolQueryBuilder getTermQueryFromList(
      Object val, String key, BoolQueryBuilder query, Map<String, Float> constraintsMap) {
    if (!((List) val).isEmpty()) {
      if (((List) val).get(0) instanceof String) {
        ((List<String>) val).replaceAll(String::toLowerCase);
        query.must(createTermsQuery(key + RAW_APPEND, (List<String>) val, constraintsMap.get(key)));
      } else {
        query.must(createTermsQuery(key, (List) val, constraintsMap.get(key)));
      }
    }
    return query;
  }

  /** Method to create EXISTS and NOT EXIST FILTER QUERY . */
  /**
   * @param entry contains operations and keys for filter
   * @param query do get updated with provided operations
   * @param constraintsMap to set ant constraints on keys for filter
   * @return
   */
  @SuppressWarnings("unchecked")
  private static BoolQueryBuilder createESOpperation(
      Entry<String, Object> entry, BoolQueryBuilder query, Map<String, Float> constraintsMap) {

    String operation = entry.getKey();
    if (entry.getValue() != null && entry.getValue() instanceof List) {
      List<String> existsList = (List<String>) entry.getValue();

      if (EsConstant.EXISTS.equalsIgnoreCase(operation)) {
        for (String name : existsList) {
          query.must(createExistQuery(name, constraintsMap.get(name)));
        }
      } else if (EsConstant.NOT_EXISTS.equalsIgnoreCase(operation)) {
        for (String name : existsList) {
          query.mustNot(createExistQuery(name, constraintsMap.get(name)));
        }
      }
    }
    return query;
  }

  /** Method to return the sorting order on basis of string param . */
  public static SortOrder getSortOrder(String value) {
    return ASC_ORDER.equalsIgnoreCase(value) ? SortOrder.ASC : SortOrder.DESC;
  }

  /**
   * This method return MatchQueryBuilder Object with boosts if any provided
   *
   * @param name of the attribute
   * @param value of the attribute
   * @param boost for increasing the search parameters priority
   * @return MatchQueryBuilder
   */
  public static MatchQueryBuilder createMatchQuery(String name, Object value, Float boost) {
    if (isNotNull(boost)) {
      return QueryBuilders.matchQuery(name, value).boost(boost);
    } else {
      return QueryBuilders.matchQuery(name, value);
    }
  }

  /**
   * This method returns TermsQueryBuilder with boosts if any provided
   *
   * @param key : field name
   * @param values : values for the field value
   * @param boost for increasing the search parameters priority
   * @return TermsQueryBuilder
   */
  private static TermsQueryBuilder createTermsQuery(String key, List values, Float boost) {
    if (isNotNull(boost)) {
      return QueryBuilders.termsQuery(key, (values).stream().toArray(Object[]::new)).boost(boost);
    } else {
      return QueryBuilders.termsQuery(key, (values).stream().toArray(Object[]::new));
    }
  }

  /**
   * This method returns RangeQueryBuilder with boosts if any provided
   *
   * @param name for the field
   * @param rangeOperation: keys and value related to range
   * @param boost for increasing the search parameters priority
   * @return RangeQueryBuilder
   */
  private static RangeQueryBuilder createRangeQuery(
      String name, Map<String, Object> rangeOperation, Float boost) {

    RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(name + RAW_APPEND);
    for (Map.Entry<String, Object> it : rangeOperation.entrySet()) {
      switch (it.getKey()) {
        case LTE:
          rangeQueryBuilder.lte(it.getValue());
          break;
        case LT:
          rangeQueryBuilder.lt(it.getValue());
          break;
        case GTE:
          rangeQueryBuilder.gte(it.getValue());
          break;
        case GT:
          rangeQueryBuilder.gt(it.getValue());
          break;
      }
    }
    if (isNotNull(boost)) {
      return rangeQueryBuilder.boost(boost);
    }
    return rangeQueryBuilder;
  }

  /**
   * This method returns TermQueryBuilder with boosts if any provided
   *
   * @param name of the field for termquery
   * @param value of the field for termquery
   * @param boost for increasing the search parameters priority
   * @return TermQueryBuilder
   */
  private static TermQueryBuilder createTermQuery(String name, Object value, Float boost) {
    if (isNotNull(boost)) {
      return QueryBuilders.termQuery(name, value).boost(boost);
    } else {
      return QueryBuilders.termQuery(name, value);
    }
  }

  /**
   * this method return ExistsQueryBuilder with boosts if any provided
   *
   * @param name of the field which required for exists operation
   * @param boost for increasing the search parameters priority
   * @return ExistsQueryBuilder
   */
  private static ExistsQueryBuilder createExistQuery(String name, Float boost) {
    if (isNotNull(boost)) {
      return QueryBuilders.existsQuery(name).boost(boost);
    } else {
      return QueryBuilders.existsQuery(name);
    }
  }

  /**
   * This method create lexical query with boosts if any provided
   *
   * @param key for search
   * @param rangeOperation to search or match in a particular way
   * @param boost for increasing the search parameters priority
   * @return QueryBuilder
   */
  public static QueryBuilder createLexicalQuery(
      String key, Map<String, Object> rangeOperation, Float boost) {
    QueryBuilder queryBuilder = null;
    for (Map.Entry<String, Object> it : rangeOperation.entrySet()) {
      switch (it.getKey()) {
        case STARTS_WITH:
          {
            String startsWithVal = (String) it.getValue();
            if (StringUtils.isNotBlank(startsWithVal)) {
              startsWithVal = startsWithVal.toLowerCase();
            }
            if (isNotNull(boost)) {
              queryBuilder =
                  QueryBuilders.prefixQuery(key + RAW_APPEND, startsWithVal).boost(boost);
            }
            queryBuilder = QueryBuilders.prefixQuery(key + RAW_APPEND, startsWithVal);
            break;
          }
        case ENDS_WITH:
          {
            String endsWithRegex = "~" + it.getValue();
            if (isNotNull(boost)) {
              queryBuilder =
                  QueryBuilders.regexpQuery(key + RAW_APPEND, endsWithRegex).boost(boost);
            }
            queryBuilder = QueryBuilders.regexpQuery(key + RAW_APPEND, endsWithRegex);
            break;
          }
      }
    }
    return queryBuilder;
  }

  /**
   * this method will take start time and subtract with current time to get the time spent in
   * millis.
   *
   * @param startTime long
   * @return long
   */
  public static long calculateEndTime(long startTime) {
    return System.currentTimeMillis() - startTime;
  }

  /**
   * This method will create searchdto on this of searchquery provided
   *
   * @param searchQueryMap Map<String,Object> contains query
   * @return SearchDto for search data in elastic search
   */
//  public static SearchDTO createSearchDTO(Map<String, Object> searchQueryMap) {
//    SearchDTO search = new SearchDTO();
//    search = getBasicBuiders(search, searchQueryMap);
//    search = setOffset(search, searchQueryMap);
//    search = getLimits(search, searchQueryMap);
//    if (searchQueryMap.containsKey(EsConstant.GROUP_QUERY)) {
//      search
//          .getGroupQuery()
//          .addAll(
//              (Collection<? extends Map<String, Object>>)
//                  searchQueryMap.get(EsConstant.GROUP_QUERY));
//    }
//    search = getSoftConstraints(search, searchQueryMap);
//    return search;
//  }

  /**
   * This method add any softconstraints present in seach query to search DTo
   *
   * @param SearchDTO search which contains the search parameters for elastic search.
   * @param Map searchQueryMap which contains soft_constraints
   * @return SearchDTO updated searchDTO which contains soft_constraits
   */
  private static SearchDTO getSoftConstraints(
      SearchDTO search, Map<String, Object> searchQueryMap) {
    if (searchQueryMap.containsKey(EsConstant.SOFT_CONSTRAINTS)) {
      // Play is converting int value to bigInt so need to convert back those data to
      // int
      // SearchDto soft constraints expect Map<String, Integer>
      Map<String, Integer> constraintsMap = new HashMap<>();
      Set<Entry<String, BigInteger>> entrySet =
          ((Map<String, BigInteger>) searchQueryMap.get(EsConstant.SOFT_CONSTRAINTS)).entrySet();
      Iterator<Entry<String, BigInteger>> itr = entrySet.iterator();
      while (itr.hasNext()) {
        Entry<String, BigInteger> entry = itr.next();
        constraintsMap.put(entry.getKey(), entry.getValue().intValue());
      }
      search.setSoftConstraints(constraintsMap);
    }
    return search;
  }

  /**
   * This method adds any limits present in the search query
   *
   * @param SearchDTO search which contains the search parameters for elastic search.
   * @param Map searchQueryMap which contain limit
   * @return SearchDTO updated searchDTO which contains limit
   */
  private static SearchDTO getLimits(SearchDTO search, Map<String, Object> searchQueryMap) {
    if (searchQueryMap.containsKey(EsConstant.LIMIT)) {
      if ((searchQueryMap.get(EsConstant.LIMIT)) instanceof Integer) {
        search.setLimit((int) searchQueryMap.get(EsConstant.LIMIT));
      } else {
        search.setLimit(((BigInteger) searchQueryMap.get(EsConstant.LIMIT)).intValue());
      }
    }
    return search;
  }

  /**
   * This method adds offset if any present in the searchQuery
   *
   * @param SearchDTO search which contains the search parameters for elastic search.
   * @param map searchQueryMap which contains offset
   * @return SearchDTO updated searchDTO which contain offset
   */
  private static SearchDTO setOffset(SearchDTO search, Map<String, Object> searchQueryMap) {
    if (searchQueryMap.containsKey(EsConstant.OFFSET)) {
      if ((searchQueryMap.get(EsConstant.OFFSET)) instanceof Integer) {
        search.setOffset((int) searchQueryMap.get(EsConstant.OFFSET));
      } else {
        search.setOffset(((BigInteger) searchQueryMap.get(EsConstant.OFFSET)).intValue());
      }
    }
    return search;
  }

  /**
   * This method adds basic query parameter to SearchDTO if any provided
   *
   * @param SearchDTO search
   * @param Map searchQueryMap
   * @return SearchDTO
   */
  private static SearchDTO getBasicBuiders(SearchDTO search, Map<String, Object> searchQueryMap) {
    if (searchQueryMap.containsKey(EsConstant.QUERY)) {
      search.setQuery((String) searchQueryMap.get(EsConstant.QUERY));
    }
    if (searchQueryMap.containsKey(EsConstant.QUERY_FIELDS)) {
      search.setQueryFields((List<String>) searchQueryMap.get(EsConstant.QUERY_FIELDS));
    }
    if (searchQueryMap.containsKey(EsConstant.FACETS)) {
      search.setFacets((List<Map<String, String>>) searchQueryMap.get(EsConstant.FACETS));
    }
    if (searchQueryMap.containsKey(EsConstant.FIELDS)) {
      search.setFields((List<String>) searchQueryMap.get(EsConstant.FIELDS));
    }
    if (searchQueryMap.containsKey(EsConstant.FILTERS)) {
      search
          .getAdditionalProperties()
          .put(EsConstant.FILTERS, searchQueryMap.get(EsConstant.FILTERS));
    }
    if (searchQueryMap.containsKey(EsConstant.EXISTS)) {
      search
          .getAdditionalProperties()
          .put(EsConstant.EXISTS, searchQueryMap.get(EsConstant.EXISTS));
    }
    if (searchQueryMap.containsKey(EsConstant.NOT_EXISTS)) {
      search
          .getAdditionalProperties()
          .put(EsConstant.NOT_EXISTS, searchQueryMap.get(EsConstant.NOT_EXISTS));
    }
    if (searchQueryMap.containsKey(EsConstant.SORT_BY)) {
      search
          .getSortBy()
          .putAll((Map<? extends String, ? extends String>) searchQueryMap.get(EsConstant.SORT_BY));
    }
    return search;
  }

  /**
   * Method returns map which contains all the request data from elasticsearch
   *
   * @param SearchResponse response from elastic search
   * @param searchDTO searchDTO which was used to search data
   * @param finalFacetList Facets provide aggregated data based on a search query
   * @return Map which will have all the requested data
   */
  public static Map<String, Object> getSearchResponseMap(
      SearchResponse response, SearchDTO searchDTO, List finalFacetList) {
    Map<String, Object> responseMap = new HashMap<>();
    List<Map<String, Object>> esSource = new ArrayList<>();
    long count = 0;
    if (response != null) {
      SearchHits hits = response.getHits();
      count = hits.getTotalHits();

      for (SearchHit hit : hits) {
        esSource.add(hit.getSourceAsMap());
      }

      // fetch aggregations aggregations
      finalFacetList = getFinalFacetList(response, searchDTO, finalFacetList);
    }
    responseMap.put(EsConstant.CONTENT, esSource);
    if (!(finalFacetList.isEmpty())) {
      responseMap.put(EsConstant.FACETS, finalFacetList);
    }
    responseMap.put(EsConstant.COUNT, count);
    return responseMap;
  }

  private static List getFinalFacetList(
      SearchResponse response, SearchDTO searchDTO, List finalFacetList) {
    if (null != searchDTO.getFacets() && !searchDTO.getFacets().isEmpty()) {

      ProjectLogger.log(
          "ElasticSearchHelper:getFinalFacetList: method start with facets not null",
          LoggerEnum.INFO);

      Map<String, String> m1 = searchDTO.getFacets().get(0);
      for (Map.Entry<String, String> entry : m1.entrySet()) {
        String field = entry.getKey();
        String aggsType = entry.getValue();
        List<Object> aggsList = new ArrayList<>();
        Map facetMap = new HashMap();
        if (EsConstant.DATE_HISTOGRAM.equalsIgnoreCase(aggsType)) {
          Histogram agg = response.getAggregations().get(field);
          for (Histogram.Bucket ent : agg.getBuckets()) {
            // DateTime key = (DateTime) ent.getKey(); // Key
            String keyAsString = ent.getKeyAsString(); // Key as String
            long docCount = ent.getDocCount(); // Doc count
            Map internalMap = new HashMap();
            internalMap.put(EsConstant.NAME, keyAsString);
            internalMap.put(EsConstant.COUNT, docCount);
            aggsList.add(internalMap);
          }
        } else {
          Terms aggs = response.getAggregations().get(field);
          for (Bucket bucket : aggs.getBuckets()) {
            Map internalMap = new HashMap();
            internalMap.put(EsConstant.NAME, bucket.getKey());
            internalMap.put(EsConstant.COUNT, bucket.getDocCount());
            aggsList.add(internalMap);
          }
        }
        facetMap.put("values", aggsList);
        facetMap.put(EsConstant.NAME, field);
        finalFacetList.add(facetMap);
      }
      /*
       * ProjectLogger.log("ElasticSearchHelper:getFinalFacetList: method end ",
       * LoggerEnum.INFO);
       */
    }
    return finalFacetList;
  }

  private static boolean isNotNull(Object object) {
    return object != null ? true : false;
  }

  /**
   * THis method will print when start time of the method
   *
   * @param message expected format of message is className:methodName: message
   * @param time current time
   */
  public static void logMethodStart(String message, long time) {
    ProjectLogger.log(message + time, LoggerEnum.PERFLOG.name());
  }
  /**
   * THis method will print when start time of the method
   *
   * @param message expected format of message is className:methodName: message
   * @param time method start time
   */
  public static void logMethodEnd(String message, long startTime) {
    ProjectLogger.log(message + calculateEndTime(startTime), LoggerEnum.PERFLOG.name());
  }
}
