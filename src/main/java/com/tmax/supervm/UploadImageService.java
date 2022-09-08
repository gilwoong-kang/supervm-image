/*
 * Copyright (c) 2022. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.tmax.supervm;

import com.google.gson.JsonObject;
import com.tmax.superobject.object.AbstractServiceObject;
import com.tmax.superobject.object.BodyObject;
import com.tmax.superobject.object.DefaultBodyObject;
import com.tmax.supervm.lib.cmd.Command;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;

public class UploadImageService extends AbstractServiceObject {
    private static final String FILE_UPLOAD_LOCN = "/Users/gilwoongkang/personalLibrary/tmax/SuperVM_Image_Service/tmp/";

    @Override
    public void service(BodyObject bodyObject) {
        /* TODO : convert SAS Body to file. needed file name, filetype(확장자), isEnd(boolean), number
         * */
        if(bodyObject.getJsonObject().get("end").getAsBoolean()){
            String target = FILE_UPLOAD_LOCN+bodyObject.getJsonObject().get("filename")+bodyObject.getJsonObject().get("filetype");
            try {
                doFileMerge(FILE_UPLOAD_LOCN,target);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // image convert.
            String imageType = checkImageType(target);

//           TODO: image convert for raw and insert rbd volume. before convert, must create rbd volume.
            switch (imageType){
                case "raw":
                // TODO: raw type image rbd import command dev needed.
                default:
                // TODO: another type image convert command dev needed.
            }
            BodyObject response = new DefaultBodyObject();
            JsonObject responseBody = new JsonObject();
            responseBody.addProperty("response","File Upload Success.");
            response.setJsonObject(responseBody);
            setReply(response);
        }else{
            System.out.println("-------------------file read---------------------");
            int filenumber = bodyObject.getJsonObject().get("number").getAsInt();
            File file = new File(FILE_UPLOAD_LOCN+bodyObject.getJsonObject().get("filename").getAsString()+String.valueOf(filenumber));
            if(!file.exists()){
                try{
                    file.createNewFile();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            try{
                Files.write(file.toPath(),bodyObject.getByteBuffer().array());
            }catch (IOException e){
                e.printStackTrace();
            }

            // TODO: response
            BodyObject response = new DefaultBodyObject();
            JsonObject responseBody = new JsonObject();
            responseBody.addProperty("response","File Upload Success.");
            response.setJsonObject(responseBody);
            setReply(response);
        }
    }

    /**
     *
     * @param dirPath tmp directory path.
     * @param target target path.
     * @return if file merge success, return true.
     * @throws IOException
     */
    private boolean doFileMerge(String dirPath, String target) throws IOException {
        File folder = new File(dirPath);
        File[] fileList = folder.listFiles();
        fileSort(fileList);
        BufferedReader input = null;
        BufferedOutputStream output = null;
        String line = null;
        byte[] data = null;
        int cnt = 0;

        try{
            output = new BufferedOutputStream(new FileOutputStream(target));
            for(File file : fileList){
                if(file.exists() && file.isFile()){
                    System.out.println("Filename : ".concat(file.toString()).toUpperCase());
                    input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                    while((line=input.readLine()) != null){
                        data = line.getBytes();
                        System.out.println("data : ".concat(line.toString()));
                        output.write(data);
                        output.write(System.lineSeparator().getBytes());
                    }
                    cnt++;
                }
            }
            System.out.println("########## FIN CNT : ".concat(String.valueOf(cnt)));
        }finally {
            if(input != null){
                try{
                    input.close();
                }catch (Exception e){
                    System.err.println("ignore: {} " + e);
                }
            }
            if(output != null){
                try{
                    output.close();
                }catch (Exception e){
                    System.err.println("ignore: {} "+ e);
                }
            }
            return true;
        }
    }
    private void fileSort(File[] filterResult){
        // 파일명으로 정렬한다. 
        Arrays.sort(filterResult, new Comparator() {
            public int compare(Object arg0, Object arg1) {
                File file1 = (File)arg0;
                File file2 = (File)arg1;
                return file1.getName().compareToIgnoreCase(file2.getName());
            }
        });
    }

    @Override
    public void completeService() {

    }

    private String checkImageType(String filepath){
        try {
            StringBuffer cmdResult = Command.getInstance().runCmd("qemu-img info "+filepath);

            String cmdString = cmdResult.toString();
            String[] strings = cmdString.split("\n");

            for(String s : strings){
                String[] r = s.split(":");
                if(r[0].equals("file format")){
                    return r[1];
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
    private String makeRbdVolume(String rbdPool, String volumeId){
        try{
            StringBuffer cmdResult = Command.getInstance().runCmd("rbd ");
        }
    }
}
