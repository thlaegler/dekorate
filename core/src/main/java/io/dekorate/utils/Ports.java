/**
 * Copyright 2018 The original authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dekorate.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.dekorate.kubernetes.config.BaseConfig;
import io.dekorate.kubernetes.config.Container;
import io.dekorate.kubernetes.config.Port;
import io.dekorate.kubernetes.config.PortBuilder;
import io.dekorate.kubernetes.config.PortFluent;
import io.fabric8.kubernetes.api.model.ContainerFluent;
import io.fabric8.kubernetes.api.model.ContainerPort;

public class Ports {

  private static final Map<String, Integer> HTTP_PORT_NAMES = Collections.unmodifiableMap(new HashMap<String, Integer>() {
    {
      put("http", 80);
      put("https", 443);
      put("http1", 80);
      put("h2c", 443);
    }
  });

  private static final Map<Integer, Integer> HTTP_PORT_NUMBERS = Collections.unmodifiableMap(new HashMap<Integer, Integer>() {
    {
      put(80, 80);
      put(8080, 80);
      put(443, 443);
      put(8443, 443);
    }
  });

  public static final String DEFAULT_HTTP_PORT_PATH = "/";

  public static final Predicate<PortBuilder> PORT_PREDICATE = p -> HTTP_PORT_NAMES.containsKey(p.getName())
      || HTTP_PORT_NAMES.containsKey(p.getName())
      || HTTP_PORT_NUMBERS.containsKey(p.getContainerPort());

  public static final Map<String, Integer> webPortNameMappings() {
    return HTTP_PORT_NAMES;
  }

  public static final List<String> webPortNames() {
    return HTTP_PORT_NAMES.keySet().stream().collect(Collectors.toList());
  }

  public static final Map<Integer, Integer> webPortNumberMappings() {
    return HTTP_PORT_NUMBERS;
  }

  public static final List<Integer> webPortNumbers() {
    return HTTP_PORT_NUMBERS.keySet().stream().collect(Collectors.toList());
  }

  public static Port populateHostPort(Port port) {
    if (!isWebPort(port)) {
      return port;
    }

    if (port.getHostPort() != null) {
      return port;
    }

    if (port.getContainerPort() != null && HTTP_PORT_NUMBERS.containsKey(port)) {
      return new PortBuilder(port).withHostPort(HTTP_PORT_NUMBERS.get(port.getContainerPort())).build();
    }

    if (port.getName() != null && HTTP_PORT_NAMES.containsKey(port.getName())) {
      return new PortBuilder(port).withHostPort(HTTP_PORT_NAMES.get(port.getName())).build();
    }
    //No match
    return port;
  }

  public static boolean isWebPort(Port port) {
    if (webPortNames().contains(port.getName())) {
      return true;
    }
    if (webPortNumbers().contains(port.getContainerPort())) {
      return true;
    }
    return false;
  }

  public static boolean isWebPort(PortFluent port) {
    if (webPortNames().contains(port.getName())) {
      return true;
    }
    if (webPortNumbers().contains(port.getContainerPort())) {
      return true;
    }
    return false;
  }

  public static Optional<ContainerPort> getHttpPort(ContainerFluent<?> container) {
    //If we have a single port, return that no matter what.
    if (container.getPorts().size() == 1) {
      return Optional.of(container.getPorts().get(0));
    }

    //Check the service name
    Optional<ContainerPort> port = container.getPorts().stream().filter(p -> HTTP_PORT_NAMES.containsKey(p.getName()))
        .findFirst();
    if (port.isPresent()) {
      return port;
    }

    port = container.getPorts().stream().filter(p -> HTTP_PORT_NUMBERS.containsKey(p.getHostPort())).findFirst();
    if (port.isPresent()) {
      return port;
    }
    return Optional.empty();
  }

  public static Optional<Port> getHttpPort(Container container) {
    //If we have a single port, return that no matter what.
    if (container.getPorts().length == 1) {
      return Optional.of(container.getPorts()[0]);
    }

    //Check the service name
    Optional<Port> port = Arrays.stream(container.getPorts()).filter(p -> HTTP_PORT_NAMES.containsKey(p.getName()))
        .findFirst();
    if (port.isPresent()) {
      return port;
    }

    port = Arrays.stream(container.getPorts()).filter(p -> HTTP_PORT_NUMBERS.containsKey(p.getHostPort())).findFirst();
    if (port.isPresent()) {
      return port;
    }
    return Optional.empty();
  }

  public static Optional<Port> getHttpPort(BaseConfig config) {
    //If we have a single port, return that no matter what.
    if (config.getPorts().length == 1) {
      return Optional.of(config.getPorts()[0]);
    }

    //Check the service name
    Optional<Port> port = Arrays.stream(config.getPorts()).filter(p -> HTTP_PORT_NAMES.containsKey(p.getName())).findFirst();
    if (port.isPresent()) {
      return port;
    }

    port = Arrays.stream(config.getPorts()).filter(p -> HTTP_PORT_NUMBERS.containsKey(p.getHostPort())).findFirst();
    if (port.isPresent()) {
      return port;
    }
    return Optional.empty();
  }
}
