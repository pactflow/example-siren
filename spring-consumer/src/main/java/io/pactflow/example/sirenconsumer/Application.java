package io.pactflow.example.sirenconsumer;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.hateoas.EntityModel;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class Application
{

	public static void main(String[] args) {
		deleteFirstOrder("http://localhost:8080");
	}
	public static boolean deleteFirstOrder(String url) {
		try {
			RestTemplate restTemplate = new RestTemplate();
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			mapper.registerModule(new Jackson2HalModule());



			String baseRequest = url + "/";
			String baseResponse = restTemplate.getForObject(baseRequest, String.class);
			EntityModel<?> baseResource = mapper.readValue(baseResponse, EntityModel.class);
			System.out.println(baseResource.getContent());
			Map<String, Object> baseResourceContent = (Map<String, Object>) baseResource.getContent();
			List<Map<String, Object>> urls = (List<Map<String, Object>>) baseResourceContent.get("links");

			// Follow 'orders' link
			String ordersUrl = urls.get(0).get("href").toString();
			String ordersResponse = restTemplate.getForObject(ordersUrl, String.class);
			EntityModel<?> ordersResource = mapper.readValue(ordersResponse, EntityModel.class);
			System.out.println(ordersResource);
			
			// find the first order, its actions, and perform delete if it exists
			Map<String, Object> ordersContent = (Map<String, Object>) ordersResource.getContent();
			List<Map<String, Object>> entities = (List<Map<String, Object>>) ordersContent.get("entities");
			List<Map<String, Object>> actions = (List<Map<String, Object>>) entities.get(0).get("actions");
			for (Map<String, Object> action : actions) {
				System.out.println("Action: " + action);
				if ("delete".equals(action.get("name"))) {
					String deleteHref = (String) action.get("href");
					System.out.println("Deleting order at: " + deleteHref);
					restTemplate.delete(deleteHref);
					System.out.println("Deleted order");
					return true;
				}
			}

			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
