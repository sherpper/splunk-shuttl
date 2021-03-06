// Copyright (C) 2011 Splunk Inc.
//
// Splunk Inc. licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.splunk.shuttl.archiver.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.splunk.shuttl.archiver.model.Bucket;
import com.splunk.shuttl.server.model.BucketBean;

public class JsonUtils {

	/**
	 * Merge a key in Json objects. Recommend viewing tests to see how the merging
	 * works.
	 */
	public static JSONObject mergeKey(List<JSONObject> jsons, String key) {
		try {
			return doMergeKey(jsons, key);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	private static JSONObject doMergeKey(List<JSONObject> jsons, String key)
			throws JSONException {
		JSONObject merged = new JSONObject();
		for (JSONObject json : jsons)
			mergeJsonAtKey(merged, json, key);
		return merged;
	}

	private static void mergeJsonAtKey(JSONObject merged, JSONObject json,
			String key) throws JSONException {
		Object value = getJsonKeyOrNull(json, key);
		if (value != null) {
			if (value instanceof JSONArray) {
				assureMergedValueIsAnArray(merged, key);
				mergeJsonArray(merged, key, (JSONArray) value);
			} else {
				appendKeyValue(merged, key, value);
			}
		}
	}

	private static void assureMergedValueIsAnArray(JSONObject merged, String key)
			throws JSONException {
		if (!merged.has(key))
			merged.put(key, Collections.emptyList());
	}

	private static Object getJsonKeyOrNull(JSONObject json, String key)
			throws JSONException {
		try {
			return json.get(key);
		} catch (JSONException e) {
			return null;
		}
	}

	private static void mergeJsonArray(JSONObject merged, String key,
			JSONArray array) throws JSONException {
		for (int i = 0; i < array.length(); i++)
			appendKeyValue(merged, key, array.get(i));
	}

	private static void appendKeyValue(JSONObject merged, String key, Object value)
			throws JSONException {
		merged.accumulate(key, value);
	}

	/**
	 * Takes a JSON, a key to sum and a key to the object within the JSON which
	 * has this key. Examples:
	 * 
	 * <pre>
	 * {} -> 0
	 * {objectKey : {keyToSum : 3}} -> 3
	 * {objectKey : [{keyToSum : 1}, {keyToSum : 4}]} -> 5
	 * </pre>
	 */
	public static long sumKeyInNestedJson(JSONObject jsonObject, String keyToSum,
			String objectKey) {
		try {
			return doSumKey(jsonObject, keyToSum, objectKey);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	private static long doSumKey(JSONObject jsonObject, String keyToSum,
			String objectKey) throws JSONException {
		long size = 0;
		if (jsonObject.has(objectKey)) {
			Object object = jsonObject.get(objectKey);
			if (object instanceof JSONArray) {
				size = sumKeyInArray((JSONArray) object, keyToSum);
			} else if (object instanceof JSONObject) {
				size = valueOrZero((JSONObject) object, keyToSum);
			} else {
				throw new RuntimeException("Unknown JSON class: " + object.getClass());
			}
		}
		return size;
	}

	private static long sumKeyInArray(JSONArray jsonArray, String key) {
		try {
			long sum = 0;
			for (int i = 0; i < jsonArray.length(); i++)
				sum += valueOrZero((JSONObject) jsonArray.get(i), key);
			return sum;
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	private static long valueOrZero(JSONObject jsonObject, String key) {
		try {
			return jsonObject.getLong(key);
		} catch (JSONException e) {
			return 0;
		}
	}

	public static JSONObject mergeJsonsWithKeys(List<JSONObject> jsons,
			String... keys) {
		JSONObject merge = new JSONObject();
		for (String key : keys)
			checkedPut(jsons, merge, key);
		return merge;
	}

	private static void checkedPut(List<JSONObject> jsons, JSONObject merge,
			String key) {
		try {
			merge.put(key, JsonUtils.mergeKey(jsons, key).get(key));
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	public static JSONObject writeKeyValueAsJson(Object... kvs) {
		JSONObject jsonObject = new JSONObject();
		for (int i = 0; i < kvs.length; i += 2) {
			Object k = kvs[i];
			Object v = kvs[i + 1];

			v = changeToBucketBeansIfValueIsAListOfBuckets(v);

			putSafe(jsonObject, k, v);
		}
		return jsonObject;
	}

	@SuppressWarnings("unchecked")
	private static Object changeToBucketBeansIfValueIsAListOfBuckets(Object v) {
		if (v instanceof List<?>) {
			List<?> list = (List<?>) v;
			if (!list.isEmpty() && list.get(0) instanceof Bucket) {
				List<Bucket> buckets = (List<Bucket>) list;
				List<BucketBean> beans = new ArrayList<BucketBean>();
				for (Bucket b : buckets)
					beans.add(BucketBean.createBeanFromBucket(b));
				v = beans;
			}
		}
		return v;
	}

	private static void putSafe(JSONObject jsonObject, Object k, Object v) {
		try {
			String key = k.toString();
			if (v instanceof Collection)
				jsonObject.put(key, (Collection<?>) v);
			else if (v instanceof Map)
				jsonObject.put(key, (Map<?, ?>) v);
			else
				jsonObject.put(key, v);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
}
