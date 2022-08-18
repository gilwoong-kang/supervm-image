

package com.tmax.supervm;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmax.superobject.object.AbstractServiceObject;
import com.tmax.superobject.object.BodyObject;
import com.tmax.superobject.object.DefaultBodyObject;
import com.tmax.supervm.lib.VDSMClient;

import java.util.UUID;

public class CreateImageService extends AbstractServiceObject {

    private final String CREATE_IMAGE_METHOD = "Volume.create";

    /**
     * Image create Service.
     * @param bodyObject storagepool id, storagedomain id, volume size, volume format,
     *                   preallocate volume type, disk type, source image UUID, source volume UUID
     * @response image UUID.
     */
    @Override
    public void service(BodyObject bodyObject){

        JsonObject params = bodyObject.getJsonObject();
        JsonObject request = new JsonObject();

        BodyObject response = new DefaultBodyObject();
        JsonObject responseBody = new JsonObject();

        JsonObject reqParams = new JsonObject();

        request.addProperty("method",CREATE_IMAGE_METHOD);
        try {
            reqParams.addProperty("volumeID",UUID.randomUUID().toString());
            reqParams.addProperty("storagepoolID",params.get("storagepoolID").getAsString());
            reqParams.addProperty("storagedomainID",params.get("storagedomainID").getAsString());
            reqParams.addProperty("imageID",UUID.randomUUID().toString());
            reqParams.addProperty("size",params.get("size").getAsString());
            reqParams.addProperty("volFormat",params.get("volFormat").getAsInt());
            reqParams.addProperty("preallocate",params.get("volType").getAsInt());
            reqParams.addProperty("diskType",params.get("diskType").getAsString());
            reqParams.add("desc",params.get("desc"));
            reqParams.addProperty("srcImgUUID",params.get("srcImgUUID").getAsString());
            reqParams.addProperty("srcVolUUID",params.get("srcVolUUID").getAsString());

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
    public void completeService(){

    }
}
