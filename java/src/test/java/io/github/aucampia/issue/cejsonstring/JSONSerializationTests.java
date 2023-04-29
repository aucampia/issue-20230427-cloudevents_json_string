package io.github.aucampia.issue.cejsonstring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Named.named;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.jackson.JsonFormat;
import io.cloudevents.jackson.JsonFormatOptions;
import io.cloudevents.jackson.JsonFormatOptions.JsonFormatOptionsBuilder;
import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class JSONSerializationTests {

  private static final Logger LOGGER = Logger.getLogger(JSONSerializationTests.class.getName());

  private static final JsonNodeFactory jnf = JsonNodeFactory.instance;

  private static final List<CaseOutput> outputs = new ArrayList<>();

  record CaseOutput(
      byte[] data,
      String dataString,
      String dataContentType,
      JsonFormatOptions options,
      JsonNode jsonOutput) {}
  ;

  record Case(
      Consumer<CloudEventBuilder> builderMutator,
      Consumer<JsonFormatOptionsBuilder> formatOptionsMutator,
      Consumer<JsonNode> nodeChecker) {
    void check() throws Exception {
      var builder =
          CloudEventBuilder.v1()
              .withId("00000000-0000-0000-0000-000000000000")
              .withType("example.event.v0")
              .withSource(URI.create("http://example.com"));

      this.builderMutator.accept(builder);
      var event = builder.build();

      var formatOptionsBuilder = JsonFormatOptions.builder();
      if (this.formatOptionsMutator != null) {
        this.formatOptionsMutator.accept(formatOptionsBuilder);
      }
      var formatOptions = formatOptionsBuilder.build();
      var format = new JsonFormat(formatOptions);

      var serialized = format.serialize(event);
      LOGGER.info(() -> String.format("serialized = %s", new String(serialized)));

      var mapper = new ObjectMapper();
      var parsed = mapper.readTree(serialized);
      LOGGER.info(() -> String.format("parsed = %s", parsed));

      var dataNode = parsed.get("data");
      this.nodeChecker.accept(dataNode);

      outputs.add(
          new CaseOutput(
              event.getData().toBytes(),
              new String(event.getData().toBytes(), StandardCharsets.UTF_8),
              event.getDataContentType(),
              formatOptions,
              parsed));
    }
  }

  private static Stream<Object> makeCases() {
    return Stream.of(
        named(
            "JSON in byte[] with 'application/json' DataContentType becomes JSON object when"
                + " serialized",
            new Case(
                (CloudEventBuilder builder) -> {
                  builder
                      .withDataContentType("application/json")
                      .withData("""
                        {"a":1,"b":2}""".getBytes());
                },
                null,
                (JsonNode dataNode) -> {
                  assertTrue(dataNode.isObject());
                  assertTrue(dataNode.has("a"));
                  assertTrue(dataNode.has("b"));
                  assertEquals(dataNode.get("a").numberValue(), 1);
                  assertEquals(dataNode.get("b").numberValue(), 2);
                })),
        named(
            "JSON in byte[] with no DataContentType becomes JSON object when serialized",
            new Case(
                (CloudEventBuilder builder) -> {
                  builder.withData("""
                              {"a":1,"b":2}""".getBytes());
                },
                null,
                (JsonNode dataNode) -> {
                  assertEquals(
                      dataNode,
                      jnf.objectNode()
                          .<ObjectNode>set("a", jnf.numberNode(1))
                          .<ObjectNode>set("b", jnf.numberNode(2)));
                })),
        named(
            "JSON in byte[] with 'text/plain' and forceStringSerialization becomes JSON object when"
                + " serialized",
            new Case(
                (CloudEventBuilder builder) -> {
                  builder
                      .withDataContentType("text/plain")
                      .withData(
                          """
                                      {"a":1,"b":2}""".getBytes());
                },
                (JsonFormatOptionsBuilder builder) -> {
                  builder.forceStringSerialization(true);
                },
                (JsonNode dataNode) -> {
                  assertEquals(dataNode, jnf.textNode("""
                    {"a":1,"b":2}"""));
                })));
  }

  @ParameterizedTest()
  @MethodSource("makeCases")
  void jsonSerialization(final Case param) throws Exception {
    param.check();
  }

  @Test
  void stuffA() {
    jnf.objectNode().setAll(Map.of("a", jnf.numberNode(1), "b", jnf.numberNode(2)));
  }

  @AfterAll
  public static void afterAll() throws Exception {
    var mapper =
        new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
    mapper.writeValue(new File("output.yaml"), outputs);
  }
}
