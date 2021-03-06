/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.verifier.messaging.stream;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.contract.verifier.messaging.MessageVerifierReceiver;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.function.StreamFunctionProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.util.StringUtils;

class StreamOutputDestinationMessageReceiver
		implements MessageVerifierReceiver<Message<?>> {

	private static final Log log = LogFactory
			.getLog(StreamOutputDestinationMessageReceiver.class);

	private final ApplicationContext context;

	StreamOutputDestinationMessageReceiver(ApplicationContext context) {
		this.context = context;
	}

	@Override
	public Message<?> receive(String destination, long timeout, TimeUnit timeUnit) {
		try {
			OutputDestination outputDestination = this.context
					.getBean(OutputDestination.class);
			StreamFunctionProperties streamFunctionProperties = this.context
					.getBean(StreamFunctionProperties.class);
			int indexOfDestination = StringUtils
					.isEmpty(streamFunctionProperties.getDefinition()) ? 0
							: indexOfDestination(streamFunctionProperties, destination);
			return outputDestination.receive(timeUnit.toMillis(timeout),
					indexOfDestination);
		}
		catch (Exception e) {
			log.error("Exception occurred while trying to read a message from "
					+ " a channel with name [" + destination + "]", e);
			throw new IllegalStateException(e);
		}
	}

	private int indexOfDestination(StreamFunctionProperties streamFunctionProperties,
			String destination) {
		String[] split = streamFunctionProperties.getDefinition().split(";");
		int indexOfDestination = Arrays.stream(split).map(String::toLowerCase)
				.collect(Collectors.toList()).indexOf(destination.toLowerCase());
		if (indexOfDestination == -1) {
			throw new IllegalStateException("Destination with name [" + destination
					+ "] not found in the function definitions ["
					+ streamFunctionProperties.getDefinition() + "]");
		}
		return indexOfDestination;
	}

	@Override
	public Message<?> receive(String destination) {
		return receive(destination, 5, TimeUnit.SECONDS);
	}

}
