/*
 * Copyright © 2016 siwind, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.siwind.sdn.opendaylight.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
//import org.opendaylight.controller.sal.binding.api.NotificationService;

import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.odlhello.rev150105.OdlHelloService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.NotificationListener;


public class OdlHelloProvider {

	private static final Logger LOG = LoggerFactory.getLogger(OdlHelloProvider.class);

	// md-sal service provider
	private final DataBroker dataBroker;
	private final RpcProviderRegistry rpcRegistry;
	private final NotificationPublishService notificationPublishService;
	private final NotificationService notificationService;
	private final org.opendaylight.controller.sal.binding.api.NotificationService notiService;
	
	// Service
	private RpcRegistration<OdlHelloService> theService = null;

	// registration for PacketProcessingListener
	private ListenerRegistration<NotificationListener> registration = null;

	public OdlHelloProvider(final DataBroker dataBroker, final RpcProviderRegistry rpcRegistry,
			final NotificationPublishService notificationPublishService,
			final NotificationService notificationService,
			final org.opendaylight.controller.sal.binding.api.NotificationService notiService) {

		this.dataBroker = dataBroker;
		this.rpcRegistry = rpcRegistry;
		this.notificationPublishService = notificationPublishService;
		this.notificationService = notificationService;
		this.notiService = notiService;
	}

	/**
	 * Method called when the blueprint container is created.
	 */
	public void init() {
		LOG.info("OdlHelloProvider Session Initiated");

		PacketProcessingService packetProcessingService = rpcRegistry.getRpcService(PacketProcessingService.class);

		theService = rpcRegistry.addRpcImplementation(OdlHelloService.class, new OdlHelloImpl(packetProcessingService));

		if (notificationService != null) {
			LOG.info("NotificationService is: " + notificationService.toString());
			HelloPacketReceivedHandler packetHandler = new HelloPacketReceivedHandler();
			registration = notificationService.registerNotificationListener(packetHandler);


		}
		if (notiService != null) {
			LOG.info("NotificationService is: " + notiService.toString());
			//HelloPacketReceivedHandler packetHandler = new HelloPacketReceivedHandler();
			//registration = notiService.registerNotificationListener(packetHandler);


		}		
		//publish a packetreceived
		//this.notificationPublishService.putNotification(new PacketReceivedBuilder().);
	}

	/**
	 * Method called when the blueprint container is destroyed.
	 */
	public void close() {
		LOG.info("OdlHelloProvider Closed");

		if (theService != null) {
			theService.close();
		}
		if (registration != null) {
			registration.close();
		}
	}
}
