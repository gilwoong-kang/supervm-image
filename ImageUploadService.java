/*
 * Copyright (c) 2022. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */
package com.tmax.supervm.services;
import com.google.gson.JsonObject;
import com.tmax.superobject.object.AbstractServiceObject;
import com.tmax.superobject.object.BodyObject;
import com.tmax.superobject.object.DefaultBodyObject;
import com.tmax.supervm.lib.DbcpConnectionManager;
import com.tmax.supervm.lib.cmd.Command;
import java.io.*;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;
public class UploadImageService extends AbstractServiceObject {
    private static final String FILE_UPLOAD_LOCN = "/root/tmp/";
    private final String RAW_IMAGE_IMPORT_COMMAND = "rbd import --image-format 2 ";
    private final String IMAGE_CONVERT_COMMAND = "qemu-img convert -f ";
    private String RBDPOOL = "rbdpool";
    @Override
    public void service(BodyObject bodyObject) {
        /* TODO convert SAS Body to file. needed file name, filetype(.xxx), isEnd(boolean), number, size
         * */
        if(bodyObject.getJsonObject().get("end").getAsBoolean()){
            String target = FILE_UPLOAD_LOCN+bodyObject.getJsonObject().get("filename").getAsString()+bodyObject.getJsonObject().get("filetype").getAsString();
            try {
                doFileMerge(FILE_UPLOAD_LOCN,target);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // image convert.
            String imageType = checkImageType(target).trim();
            String volumeID = "volume-"+UUID.randomUUID().toString();
            switch (imageType){
                case "raw":
                    System.out.println(importRawImage(RBDPOOL,volumeID,target));
                default:
                    System.out.println(convertImage(RBDPOOL,volumeID,target,imageType));
            }
            // TODO insert volume data to storage DB, change DB connect manager.
            try {
                Connection conn = DbcpConnectionManager.getConnection();
                Statement stmt = conn.createStatement();
                stmt.executeQuery(" INSERT INTO TEST_IMAGE VALUES ('"+UUID.randomUUID().toString()+"','"+RBDPOOL+"','"+volumeID+"',"+bodyObject.getJsonObject().get("size").getAsInt()+"); ");
                System.out.println("insert success.");
            } catch (SQLException e) {
                throw new RuntimeException(e);
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
//            String[] cmd = {"/bin/bash","-c","\"qemu-img","info",filepath+"\""};
            StringBuffer cmdResult = Command.getInstance().runCmdString("qemu-img info "+filepath);
//            StringBuffer cmdResult = Command.getInstance().runCmd(cmd);
            String cmdString = cmdResult.toString();
            String[] strings = cmdString.split("\n");
            for(String s : strings){
                System.out.println("Line : "+s);
                String[] r = s.split(":");
                if(r[0].trim().equals("file format")){
                    System.out.println(r[1]);
                    return r[1];
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
    private String importRawImage(String rbdpool, String volumeId, String imagePath){
        try{
            StringBuffer cmdResult = Command.getInstance().runCmdString(RAW_IMAGE_IMPORT_COMMAND+imagePath+" "+rbdpool+"/"+volumeId);
            return cmdResult.toString();
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
    private String convertImage(String rbdpool, String volumeId, String imagePath, String imageType){
        try{
            StringBuffer cmdResult = Command.getInstance().runCmdString(IMAGE_CONVERT_COMMAND+imageType+" -O raw "+imagePath+" rbd:"+rbdpool+"/"+volumeId);
            return cmdResult.toString();
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
