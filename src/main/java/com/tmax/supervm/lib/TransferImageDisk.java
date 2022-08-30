/*
 * Copyright (c) 2022. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.tmax.supervm.lib;

import com.tmax.supervm.entity.ImageTicket;

import java.util.UUID;

public class TransferImageDisk {
    private final int ticketLifeTime = 1800;

    private ImageTicket buildImageTicket(UUID ticketId, String ticketUrl) {
        ImageTicket ticket = new ImageTicket();

        ticket.setId(ticketId);
        ticket.setTimeout(getClientTicketLifetime());
        ticket.setSize(10240);
        ticket.setUrl("file:///rhev/data-center/3a4b3288-16ba-11ed-a75f-00163e0ca64a/79e50515-787e-4333-86e6-53d14acc29c7/images/4a895e48-c1b7-43a2-b1e0-293b6765780a/13180d34-9b26-48fc-8c13-3078513e1173");
//        ticket.setTransferId(getParameters().getCommandId().toString());
//        ticket.setFilename(getParameters().getDownloadFilename());
//        ticket.setSparse(isSparseImage());
//        ticket.setDirty(isSupportsDirtyExtents());
        ticket.setOps(new String[] {"read","write"});

        return ticket;
    }
    private int getClientTicketLifetime(){
        return ticketLifeTime * 1000;
    }
}
