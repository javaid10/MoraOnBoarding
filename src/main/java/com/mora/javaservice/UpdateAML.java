package com.mora.javaservice;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.dbp.core.error.DBPApplicationException;
import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

public class UpdateAML implements JavaService2 {

	@Override
	public Object invoke(String arg0, Object[] arg1, DataControllerRequest request, DataControllerResponse response)
			throws Exception {

		Result result = new Result();

		HashMap<String, Object> input = new HashMap<String, Object>();
		if (preProcess(request, response)) {
			if (request.getParameter("Operation").equalsIgnoreCase("insert")
					&& request.getParameter("List").equalsIgnoreCase("taliban")) {
				JSONObject talObj = getTalibanList(request, response);
				if (talObj.length() != 0) {
					JSONArray jsonArray = (JSONArray) talObj.get("INDIVIDUALS");

					for (Object object : jsonArray) {
						JSONObject record = (JSONObject) object;
						HashMap<String, Object> reqParam = new HashMap();
						if(record.has("dataid")) {
							String dataid = (String) record.get("dataid");
							reqParam.put("dataid", dataid);
						} if(record.has("first_name")) {
							String first_name = (String) record.get("first_name");
							reqParam.put("firstname", first_name);
						} if(record.has("second_name")) {
							String second_name = (String) record.get("second_name");
							reqParam.put("secondname", second_name);
						} if(record.has("third_name")) {
							String third_name = (String) record.get("third_name");
							reqParam.put("thirdname", third_name);
						} if(record.has("fourth_name")) {
							String fourth_name = (String) record.get("fourth_name");
							reqParam.put("fourthname", fourth_name);
						} if(record.has("listed_on")) {
							String listed_on = (String) record.get("listed_on");
							reqParam.put("listedon", listed_on);
						}

						String res = DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices")
								.withOperationId("dbxdb_talibanlist_create").withRequestParameters(reqParam).build()
								.getResponse();

					}
				}
			} else if (request.getParameter("Operation").equalsIgnoreCase("update")
					&& request.getParameter("List").equalsIgnoreCase("taliban")) {

			} else if (request.getParameter("Operation").equalsIgnoreCase("insert")
					&& request.getParameter("List").equalsIgnoreCase("alqaida")) {
				JSONObject talObj = getAlqList(request, response);
				if (talObj.length() != 0) {
					JSONArray jsonArray = (JSONArray) talObj.get("INDIVIDUALS");

					for (Object object : jsonArray) {
						JSONObject record = (JSONObject) object;
						HashMap<String, Object> reqParam = new HashMap();
						if(record.has("dataid")) {
							String dataid = (String) record.get("dataid");
							reqParam.put("dataid", dataid);
						} if(record.has("first_name")) {
							String first_name = (String) record.get("first_name");
							reqParam.put("firstname", first_name);
						} if(record.has("second_name")) {
							String second_name = (String) record.get("second_name");
							reqParam.put("secondname", second_name);
						} if(record.has("third_name")) {
							String third_name = (String) record.get("third_name");
							reqParam.put("thirdname", third_name);
						} if(record.has("fourth_name")) {
							String fourth_name = (String) record.get("fourth_name");
							reqParam.put("fourthname", fourth_name);
						} if(record.has("listed_on")) {
							String listed_on = (String) record.get("listed_on");
							reqParam.put("listedon", listed_on);
						}

						String res = DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices")
								.withOperationId("dbxdb_alqaidalist_create").withRequestParameters(reqParam).build()
								.getResponse();

					}
				}

			} else if (request.getParameter("Operation").equalsIgnoreCase("update")
					&& request.getParameter("List").equalsIgnoreCase("alqaida")) {

			}

		}

		return null;
	}

	private JSONObject getTalibanList(DataControllerRequest request, DataControllerResponse response)
			throws DBPApplicationException {
		// Service name SCSanctions
		// operation Taliban
		HashMap<String, Object> input = new HashMap<String, Object>();

		String res = DBPServiceExecutorBuilder.builder().withServiceId("SCSanctions").withOperationId("Taliban")
				.withRequestParameters(input).build().getResponse();

		JSONObject jsonObject = new JSONObject(res);

		return jsonObject;
	}

	private JSONObject getAlqList(DataControllerRequest request, DataControllerResponse response)
			throws DBPApplicationException {
		// Service name SCSanctions
		// operation Taliban
		HashMap<String, Object> input = new HashMap<String, Object>();

		String res = DBPServiceExecutorBuilder.builder().withServiceId("SCSanctions").withOperationId("ALQaida")
				.withRequestParameters(input).build().getResponse();

		JSONObject jsonObject = new JSONObject(res);

		return jsonObject;
	}

	private boolean preProcess(DataControllerRequest request, DataControllerResponse response) {

		if (request.getParameter("Operation").isEmpty()) {
			return false;
		} else if (request.getParameter("List").isEmpty()) {
			return false;
		} else {
			return true;
		}

	}

}
