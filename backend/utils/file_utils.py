import json
import random
import string


def build_file_name(*tokens, ext=None):
    _random_string = "".join(random.choices(string.ascii_uppercase, k=5))
    return ("_".join(tokens) if tokens else _random_string) + (
        ".%s" % ext if ext else ""
    )


def dump_json_to_file(json_str, file_path):
    with open(file_path, "w+") as f:
        json.dump(json_str, f)


def read_json_file(file_path):
    with open(file_path, "r") as json_data:
        data = json.load(json_data)
        return data
