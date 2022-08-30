/*
 * Copyright (c) 2022. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.tmax.supervm;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmax.superobject.object.AbstractServiceObject;
import com.tmax.superobject.object.BodyObject;
import com.tmax.superobject.object.DefaultBodyObject;
import com.tmax.supervm.lib.VDSMClient;

public class AttachManagedVolumeService extends AbstractServiceObject {
    private final String ATTACH_MV_METHOD = "ManagedVolume.attach_volume";

    @Override
    public void service(BodyObject bodyObject) {
        JsonObject params = bodyObject.getJsonObject();
        JsonObject request = new JsonObject();

        BodyObject response = new DefaultBodyObject();
        JsonObject responseBody = new JsonObject();

        JsonObject reqParams = new JsonObject();

        request.addProperty("method",ATTACH_MV_METHOD);
        try {
            reqParams.addProperty("vol_id",params.get("vol_id").getAsString());
            reqParams.add("connection_info",params.get("connection_info"));
            request.add("params",reqParams);

            JsonElement result = VDSMClient.getInstance().start(request);
            responseBody.add("response",result);
            response.setJsonObject(responseBody);
            setReply(response);
        }catch (NullPointerException e){
            System.err.println("Some parameter is Null. ");
            responseBody.addProperty("err","Some parameter is Null");
            responseBody.addProperty("inputParams",params.toString());
            response.setJsonObject(responseBody);
            setReply(response);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void completeService() {

    }
}
