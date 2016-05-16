/*
 *    © Copyright 2016 IBM Corp.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.ibm.mobilefirst.mobileedge.js;

import org.json.JSONObject;
import org.liquidplayer.webkit.javascriptcore.JSContext;
import org.liquidplayer.webkit.javascriptcore.JSException;
import org.liquidplayer.webkit.javascriptcore.JSValue;

/**
 * Engine to execute JS code
 */
public class JSEngine {

    private static JSEngine instance = new JSEngine();

    JSContext jsContext;

    public static JSEngine getInstance() {
        return instance;
    }

    private JSEngine() {
        jsContext = new JSContext();
    }

    public void evaluateScript(String script){
        try {
            jsContext.evaluateScript(script);
        } catch (JSException e) {
            e.printStackTrace();
        }
    }

    public JSONObject executeFunction(String functionName, JSONObject params){
        JSONObject jsonResult = null;

        try {
            jsContext.evaluateScript(String.format("var jsFunctionExecutionResult = %s(%s)",functionName, params.toString()));
            JSValue result = jsContext.property("jsFunctionExecutionResult");
            jsonResult = new JSONObject(result.toJSON());
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return jsonResult;
    }
}
