package com.tmax.supervm;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmax.superobject.object.AbstractServiceObject;
import com.tmax.superobject.object.BodyObject;
import com.tmax.superobject.object.DefaultBodyObject;
import com.tmax.supervm.lib.VDSMClient;

import java.util.UUID;

public class CopyImageService extends AbstractServiceObject {
    private final String COPY_IMAGE_METHOD = "Volume.copy";

    /**
     * Image copy service.
     * @param bodyObject volume id, storage pool id, storage domain id, image id, destination storage domain id, destination image id,
     *                   destination volume id, desc, volume type, volume format, preallocate, post zero, force.
     *                   all of id is UUID format (String)
     *                   volume type, volume format, preallocate is integer.
     *                   force is boolean.
     * @response copy image UUID.
     */
    @Override
    public void service(BodyObject bodyObject) {

        JsonObject params = bodyObject.getJsonObject();
        JsonObject request = new JsonObject();

        BodyObject response = new DefaultBodyObject();
        JsonObject responseBody = new JsonObject();

        JsonObject reqParams = new JsonObject();

        request.addProperty("method",COPY_IMAGE_METHOD);
        try {
            reqParams.addProperty("volumeID", UUID.randomUUID().toString());
            reqParams.addProperty("storagepoolID",params.get("storagepoolID").getAsString());
            reqParams.addProperty("storagedomainID",params.get("storagedomainID").getAsString());
            reqParams.addProperty("imageID",UUID.randomUUID().toString());
            reqParams.addProperty("dstSdUUID",params.get("dstSdUUID").getAsString());
            reqParams.addProperty("dstImgUUID",params.get("dstImgUUID").getAsString());
            reqParams.addProperty("dstVolUUID",params.get("dstVolUUID").getAsString());
            reqParams.add("desc",params.get("desc"));
            reqParams.addProperty("volType",params.get("volType").getAsInt());
            reqParams.addProperty("volFormat",params.get("volFormat").getAsInt());
            reqParams.addProperty("preallocate",params.get("preallocate").getAsInt());
            reqParams.addProperty("postZero",params.get("postZero").getAsString());
            reqParams.addProperty("force",params.get("force").getAsBoolean());

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
