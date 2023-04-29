import base64
import json
import logging

from cloudevents import conversion

from cloudevents.http.event import CloudEvent


def test_json_serialize_string() -> None:
    event = CloudEvent.create(
        {
            "specversion": "1.0",
            "type": "example.type",
            "source": "example/uri",
            "datacontenttype": "application/json",
        },
        data='{"Hello": "there"}',
    )

    logging.debug("event = %s", event)

    event_json = conversion.to_json(event)

    logging.debug("event_json = %s", event_json)

    parsed = json.loads(event_json)

    logging.debug("parsed['data'] = %s", parsed["data"])
    assert parsed["data"] == '{"Hello": "there"}'


def test_json_serialize_bytes() -> None:
    event = CloudEvent.create(
        {
            "specversion": "1.0",
            "type": "example.type",
            "source": "example/uri",
            "datacontenttype": "application/json",
        },
        data=b'{"Hello": "there"}',
    )

    logging.debug("event = %s", event)

    event_json = conversion.to_json(event)

    logging.debug("event_json = %s", event_json)

    parsed = json.loads(event_json)
    logging.debug("parsed['data_base64'] = %s", parsed["data_base64"])
    assert parsed["data_base64"] == base64.b64encode(b'{"Hello": "there"}').decode()


def test_json_serialize_dict() -> None:
    event = CloudEvent.create(
        {
            "specversion": "1.0",
            "type": "example.type",
            "source": "example/uri",
            "datacontenttype": "application/json",
        },
        data={"Hello": "there"},
    )

    logging.debug("event = %s", event)

    event_json = conversion.to_json(event)

    logging.debug("event_json = %s", event_json)

    parsed = json.loads(event_json)
    logging.debug("parsed['data'] = %s", parsed["data"])
    assert parsed["data"] == {"Hello": "there"}
