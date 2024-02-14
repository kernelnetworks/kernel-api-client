package co.kernelnetworks.kernel.client;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import org.json.JSONObject;

import javax.naming.AuthenticationException;

class KernelApiClient {

    static class AuthorizationObject {
        public String userId;
        public String authorization;
        public Boolean expired;
    }

    static class FilterObject {
        Timestamp startDate;		   // earliest “Interaction Date”
        Timestamp endDate;  		   // latest “Interaction Date”
        Set<String> contactReputations;   // contact recognition level
        Set<String> contactRoles;	   // contact primary roles
        Set<String> institutionTypes;	   // institution types
        Set<String> provinces;   		   // province codes
        Set<String> countries;    		   // country codes

        public Timestamp getStartDate() {
            return startDate;
        }

        public Timestamp getEndDate() {
            return endDate;
        }

        public Set<String> getContactReputations() {
            return contactReputations;
        }

        public Set<String> getContactRoles() {
            return contactRoles;
        }

        public Set<String> getInstitutionTypes() {
            return institutionTypes;
        }

        public Set<String> getProvinces() {
            return provinces;
        }

        public Set<String> getCountries() {
            return countries;
        }
    }

    protected static ObjectMapper getJsonMapper() {
        AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
        ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(primary);
        mapper.setDateFormat(new StdDateFormat());
        // Eliminates nuisance json null types:
        mapper.setSerializationInclusion(Include.NON_NULL);
        return mapper;
    }

    // first parameter is email, second parameter is password, third parameter is hostname
    public static void main(String[] args) throws Exception {
        if (args.length < 3)
            throw new IllegalArgumentException("must provide email, password and hostname");
        String email = args[0];
        String password = args[1];
        String hostname = args[2];
        HttpClient client = HttpClient.newHttpClient();
        String formData = "email=" + URLEncoder.encode(email, StandardCharsets.UTF_8) + "&" + "password=" + URLEncoder.encode(password, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://" + hostname + "/kernelAPI/v2/user/authorize"))
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 201) {
            JSONObject auth = new JSONObject(response.body());
            String token = auth.optString("authorization", null);
            if (token == null)
                throw new AuthenticationException("No authentication string returned");
            FilterObject fobject = new FilterObject();
            fobject.startDate = Timestamp.valueOf("2020-08-10 07:00:00.000");
            fobject.endDate = Timestamp.valueOf("2020-11-11 07:00:00.000");
            String filters = getJsonMapper().writer().writeValueAsString(fobject);
            String queryParams = "?types=Insights&types=InitiativeResponses";
            request = HttpRequest.newBuilder()
                    .uri(URI.create("https://" + hostname + "/kernelAPI/v2/storage/topicExportAsCSV" + queryParams))
                    .header("Authorization", token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(filters))
                    .build();
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200)
                System.out.println("got valid API response:\n" + response.body());
            else
                System.out.println("request failed with status: " + response.statusCode() + "message: " + response.body());
        } else
            System.out.println("authentication request failed with status: " + response.statusCode());

    }
}

