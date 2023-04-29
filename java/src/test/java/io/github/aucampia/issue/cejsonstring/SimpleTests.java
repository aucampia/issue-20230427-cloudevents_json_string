package io.github.aucampia.issue.cejsonstring;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.jackson.JsonFormat;
import io.cloudevents.jackson.JsonFormatOptions;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

public class SimpleTests {
  private static final Logger LOGGER = Logger.getLogger(SimpleTests.class.getName());
  private static final JsonNodeFactory jnf = JsonNodeFactory.instance;

  @Test
  void jsonSerializeString() throws Exception {

    var event =
        CloudEventBuilder.v1()
            .withId("Issue1186")
            .withType("example.type")
            .withSource(URI.create("example/uri"))
            .withDataContentType("application/json")
            .withData("{\"Hello\": \"there\"}".getBytes(StandardCharsets.UTF_8))
            .build();

    var formatOptions = JsonFormatOptions.builder().forceStringSerialization(true).build();
    var format = new JsonFormat(formatOptions);

    var serialized = format.serialize(event);
    LOGGER.info(() -> String.format("serialized = %s", new String(serialized)));

    var mapper = new ObjectMapper();
    var parsed = mapper.readTree(serialized);
    LOGGER.info(() -> String.format("parsed = %s", parsed));

    var dataNode = parsed.get("data");
    assertEquals(jnf.objectNode().<ObjectNode>set("Hello", jnf.textNode("there")), dataNode);
  }
}
