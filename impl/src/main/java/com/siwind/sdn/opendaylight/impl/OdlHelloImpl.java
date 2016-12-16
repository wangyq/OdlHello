/*
 * Copyright © 2016 siwind, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.siwind.sdn.opendaylight.impl;

import java.util.concurrent.Future;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.odlhello.rev150105.SendPacketInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.odlhello.rev150105.SendPacketOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.odlhello.rev150105.SendPacketOutputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.odlhello.rev150105.OdlHelloService;

import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;

import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInputBuilder;

import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OdlHelloImpl implements OdlHelloService {

	private static final Logger LOG = LoggerFactory.getLogger(OdlHelloImpl.class);
	
	private final PacketProcessingService packetProcessingService;

	/**
	 * 
	 * @param packetProcessingService
	 */
	public OdlHelloImpl(PacketProcessingService packetProcessingService) {
		this.packetProcessingService = packetProcessingService;
	}

	@Override
	public Future<RpcResult<SendPacketOutput>> sendPacket(SendPacketInput input) {
		SendPacketOutputBuilder helloBuilder = new SendPacketOutputBuilder();
		
        NodeConnectorRef egress = input.getEgress();
        NodeRef node = input.getNode();
        byte[] payload = input.getRawpacket();

        packetOut(node,egress, payload);  //send packet!!
		
		//helloBuilder.setResult("Send Packet to " + strEgress + " OK."); //return message
		helloBuilder.setResult("Send Packet OK."); //return message
		return RpcResultBuilder.success(helloBuilder.build()).buildFuture();
	}

	// =========================================================
	
	private void packetOut(NodeRef egressNodeRef, NodeConnectorRef egressNodeConnectorRef, byte[] payload) {
        
        LOG.debug("Siwind Send packet of size {} out of port {}", payload.length, egressNodeConnectorRef);

        //Construct input for RPC call to packet processing service
        TransmitPacketInput input = new TransmitPacketInputBuilder()
                .setPayload(payload)
                .setNode(egressNodeRef)
                .setEgress(egressNodeConnectorRef)
                .build();
        packetProcessingService.transmitPacket(input);
    }    
	
	/**
	 * Sends the specified packet on the specified port.
	 *
	 * @param payload
	 *            The payload to be sent.
	 * @param ingress
	 *            The NodeConnector where the payload came from.
	 * @param egress
	 *            The NodeConnector where the payload will go.
	 */
	public void sendPacketOut(byte[] payload, NodeConnectorRef ingress, NodeConnectorRef egress) {
		if (ingress == null || egress == null)
			return;
		InstanceIdentifier<Node> egressNodePath = getNodePath(egress.getValue());
		TransmitPacketInput input = new TransmitPacketInputBuilder() //
				.setPayload(payload) //
				.setNode(new NodeRef(egressNodePath)) //
				.setEgress(egress) //
				.setIngress(ingress) //
				.build();
		packetProcessingService.transmitPacket(input);
	}

	private InstanceIdentifier<Node> getNodePath(final InstanceIdentifier<?> nodeChild) {
		return nodeChild.firstIdentifierOf(Node.class);
	}
}
