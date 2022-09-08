/*
 * Copyright (c) 2022. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.tmax.supervm.lib.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Command {
    private static Command command;

    private Command(){

    }

    public static Command getInstance(){
        if(command == null){
            command = new Command();
        }
        return command;
    }

    public StringBuffer runCmd(String cmd) throws IOException {
        Process p = Runtime.getRuntime().exec("/bin/bash -c " + cmd);

        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String l = null;
        StringBuffer sb = new StringBuffer();
        sb.append(cmd);

        while ((l = r.readLine()) != null) {
            sb.append(l);
            sb.append("\n");
            System.out.println(l);
        }
        System.out.println("toString : " + sb.toString());
        return sb;
    }

}
