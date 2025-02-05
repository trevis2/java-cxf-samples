package net.glenmazza.sfclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import net.glenmazza.sfclient.model.RecordCreateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Service to perform CRUD operations on Salesforce records:
 * https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/using_resources_working_with_records.htm
 */
@Service
@ConditionalOnProperty(name = "salesforce.client.enabled", matchIfMissing = true)
public class SalesforceRecordManager extends AbstractRESTService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SalesforceRecordManager.class);

    public SalesforceRecordManager(WebClient webClient) {
        super(webClient);
    }

    @Value("${salesforce.api.version:v56.0}")
    private String apiVersion;

    private final Map<Class<?>, JavaType> javaTypeMap = new HashMap<>();

    /**
     * Update a record in the Salesforce Database using a POJO.
     * WARNING: Any null field in the POJO will be erased in Salesforce, if updating just a few
     * fields use an object containing just those fields, or supply a Map<String, Object> instead.
     *
     * @param entity: Salesforce entity (Account, Contact, User, etc.) being updated
     * @param salesforceId: ID of Salesforce record being updated
     * @param object: Class or Map containing JUST those fields that are to be updated.
     */
    public void update(String entity, String salesforceId, Object object) throws JsonProcessingException {

        // first parsing to JSON to avoid problems working with LocalDates.
        String jsonString = objectMapper.writeValueAsString(object);

        webClient
                .patch()
                .uri(baseUrl + "/services/data/" + apiVersion + "/sobjects/" + entity + "/" + salesforceId)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(jsonString)
                .retrieve()
                .bodyToMono(String.class)
                // retry of 1: if access token expired, will be removed after
                // first failed call and obtained & used during second.
                // Can confirm by revoking token in Salesforce (Setup: Security: Session Management screen)
                // and comparing results vs. retry of 0
                .retry(1)
                .block();
    }

    /**
     * Create a record in the Salesforce Database using a POJO or a Map<String, Object>.
     * The columns of the POJO must match those of the field names for the Salesforce entity,
     * advisable to use @JsonProperty("emailaddress__c"), etc. annotations
     *
     * @param entity: Salesforce entity (Account, Contact, User, etc.) being created
     * @param object: Class or Map containing all fields required for the object
     * @return RecordCreateResponse: Salesforce object containing create results (success or error)
     */
    public RecordCreateResponse create(String entity, Object object) throws JsonProcessingException {

        // first parsing to JSON to avoid problems working with LocalDates.
        String jsonString = objectMapper.writeValueAsString(object);

        return webClient
                .post()
                .uri(baseUrl + "/services/data/" + apiVersion + "/sobjects/" + entity)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonString)
                .retrieve()
                .bodyToMono(RecordCreateResponse.class)
                // retry of 1: if access token expired, will be removed after
                // first failed call and obtained & used during second.
                // Can confirm by revoking token in Salesforce (Setup: Security: Session Management screen)
                // and comparing results vs. retry of 0
                .retry(1)
                .block();
    }

    /**
     * Delete a record in the Salesforce Database by its Salesforce Id.
     *
     * @param entity: Salesforce entity (Account, Contact, User, etc.) being deleted
     * @param salesforceId: ID of Salesforce record being delete
     */
    public void deleteObject(String entity, String salesforceId) {
        webClient.delete()
                .uri(baseUrl + "/services/data/" + apiVersion + "/sobjects/" + entity + "/" + salesforceId)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .toBodilessEntity()
                // retry of 1: if access token expired, will be removed after
                // first failed call and obtained & used during second.
                // Can confirm by revoking token in Salesforce (Setup: Security: Session Management screen)
                // and comparing results vs. retry of 0
                .retry(1)
                .block();
    }

    /**
     * Obtain all information about a particular entity in the form of a JSON-formatted String.
     *
     * @param entity - type of object being queried: Account, Contact, User, etc.
     * @param salesforceId - Salesforce ID of the object being queried.
     * @return String holding the JSON of the object queried.
     */
    public String getJson(String entity, String salesforceId) {
        return webClient
                .get()
                .uri(baseUrl + "/services/data/" + apiVersion + "/sobjects/" + entity + "/" + salesforceId)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(String.class)
                // retry of 1: if access token expired, will be removed after
                // first failed call and obtained & used during second.
                // Can confirm by revoking token in Salesforce (Setup: Security: Session Management screen)
                // and comparing results vs. retry of 0
                .retry(1)
                .block();
    }

    /**
     * Obtain information about a particular entity in a Java object.  Only information
     * returned are those with fields in that object.
     *
     * @param entity - type of object being queried: Account, Contact, User, etc.
     * @param salesforceId - Salesforce ID of the object being queried.
     * @param returnedJavaClass - Class of the POJO to return.
     * @return String holding the JSON of the object queried.
     */
    public <T> T getObject(String entity, String salesforceId, Class<?> returnedJavaClass) throws JsonProcessingException {
        JavaType type = javaTypeMap.computeIfAbsent(returnedJavaClass, this::createJavaType);
        String jsonResult = getJson(entity, salesforceId);
        return objectMapper.readValue(jsonResult, type);
    }

}
