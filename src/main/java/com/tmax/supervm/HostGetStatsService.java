package com.tmax.supervm;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmax.superobject.object.AbstractServiceObject;
import com.tmax.superobject.object.BodyObject;
import com.tmax.superobject.object.DefaultBodyObject;
import com.tmax.supervm.lib.VDSMClient;

public class HostGetStatsService extends AbstractServiceObject {
    @Override
    public void service(BodyObject bodyObject){
        JsonObject request = new JsonObject();

        BodyObject response = new DefaultBodyObject();
        JsonObject responseBody = new JsonObject();

        request.addProperty("method","Host.getStats");
        try {
            JsonElement result = VDSMClient.getInstance().start(request);

            responseBody.add("response",result);
            response.setJsonObject(responseBody);
            setReply(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void completeService(){

    }
}
