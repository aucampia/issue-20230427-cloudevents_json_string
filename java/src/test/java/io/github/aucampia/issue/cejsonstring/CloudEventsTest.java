package io.github.aucampia.issue.cejsonstring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.provider.EventFormatProvider;
import io.cloudevents.jackson.JsonFormat;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

public class CloudEventsTest {

  private static final Logger LOGGER = Logger.getLogger(CloudEventsTest.class.getName());

  @Test
  void stuff() throws Exception {
    var objectMapper = new ObjectMapper();
    var jnf = JsonNodeFactory.instance;

    var node = jnf.objectNode();
    node.set("a", jnf.numberNode(1));
    node.set("b", jnf.numberNode(2));
    var data = objectMapper.writeValueAsString(node).getBytes(StandardCharsets.UTF_8);

    CloudEvent event =
        CloudEventBuilder.v1()
            .withId("urn:fdc:aucampia.github.io:20230428:000")
            .withType("example.vertx")
            .withSource(URI.create("http://localhost"))
            .withDataContentType("application/json")
            .withData()
            .build();

    byte[] serialized =
        EventFormatProvider.getInstance().resolveFormat(JsonFormat.CONTENT_TYPE).serialize(event);
    LOGGER.info(() -> String.format("serialized = %s", new String(serialized)));
  }
}
