import json
from urllib.parse import urlparse, urljoin

from bson import json_util, ObjectId
from flask import request, g, Blueprint, Response
from flask_jwt_extended import (
    jwt_required,
    get_jwt_identity
)
from flask_login import current_user

from backend import LOGIN_MANAGER
from ..db.models import User
from ..db.models import Configuration
import time

bp = Blueprint("configuration", __name__, url_prefix="/api/configuration")


def jsonify(obj):
    content = json.dumps(obj, default=json_util.default)
    return Response(content, 200, mimetype="application/json")


# =====================
#   REST API methods
# =====================
@bp.route("/server_time", methods=["GET"])
def get_server_time():
    timestamp_in_millis = int(time.time() * 1000)
    response = jsonify({"time": timestamp_in_millis})
    return response


@LOGIN_MANAGER.user_loader
def load_user(user_id):
    return User.find_by_id(user_id)


@bp.before_request
def get_current_user():
    g.user = current_user


@bp.route("/new_config", methods=["POST"])
@jwt_required
def create_new_config():
    json = request.json
    title = json["title"]
    creator = get_jwt_identity()
    high = json["high"]
    medium = json["medium"]
    low = json["low"]

    courseCode = json["courseCode"]
    exerciseNum = json["exerciseNum"]

    existingWithTitle = Configuration.find_config_by_title(creator, title)
    if (existingWithTitle.count() > 0):
        return Response({"success": False}, 409, mimetype="application/json")

    config = Configuration(title=title,
                           creator=creator,
                           high=high,
                           medium=medium,
                           low=low,
                           courseCode=courseCode,
                           exerciseNum=exerciseNum).save()

    return Response({"success": True}, 200, mimetype="application/json")


@bp.route("/get_my_configs", methods=["GET"])
@jwt_required
def get_my_configs():
    username = get_jwt_identity()
    configs = Configuration.find_configs_by_username(username)

    titles = []
    for config in configs:
        titles.append({
            "_id": config["_id"],
            "title": config["title"],
            "courseCode": config["courseCode"],
            "exerciseNum": config["exerciseNum"]
        })

    return jsonify(titles)


@bp.route("/get_checks_for_download/<config_id>", methods=["GET"])
def get_checks_for_download(config_id):
    config = Configuration.find_config_by_id(ObjectId(config_id))

    checks = {"high": list(), "medium": list(), "low": list()}

    forFile = {
        # TODO 'config-1': '==-string',
        'config-2': 'inheritance',
        'config-3': 'no-inheritance',
        'config-4': 'interfaces',
        'config-5': 'no-interfaces',
        # TODO 'config-6': 'streams',
        # TODO 'config-7': 'no-streams',
        # TODO 'config-8': 'for-loops',
        # TODO 'config-9': 'no-for-loops',
        # TODO 'config-10': 'while-loops',
        # TODO 'config-11': 'no-while-loops',
        'config-12': 'camelcase',
        'config-13': 'screaming-snake-case',
        'config-14': 'redundant-else',
        'config-15': 'single-char-name',
        'config-16': 'method-length',
        'config-17': 'clone'
    }

    for highCheck in config["high"]:
        checks["high"].append(forFile[highCheck["check"]])

    for mediumCheck in config["medium"]:
        checks["medium"].append(forFile[mediumCheck["check"]])

    for lowCheck in config["low"]:
        checks["low"].append(forFile[lowCheck["check"]])

    return jsonify(checks)


@bp.route("/get_checks/<config_id>", methods=["GET"])
def get_checks(config_id):
    config = Configuration.find_config_by_id(ObjectId(config_id))

    checks = {
        "high": list(),
        "medium": list(),
        "low": list(),
        "title": config["title"],
        "courseCode": config["courseCode"],
        "exerciseNum": config["exerciseNum"]
    }

    for highCheck in config["high"]:
        checks["high"].append(highCheck["check"])

    for mediumCheck in config["medium"]:
        checks["medium"].append(mediumCheck["check"])

    for lowCheck in config["low"]:
        checks["low"].append(lowCheck["check"])

    return jsonify(checks)


@bp.route("/<config_id>", methods=["DELETE"])
def delete_config(config_id):
    return jsonify(Configuration.delete_by_id(ObjectId(config_id)))


##################################################################
# U T I L I T I E S
##################################################################
def is_safe_url(request_host_url, target):
    ref_url = urlparse(request_host_url)
    test_url = urlparse(urljoin(request_host_url, target))
    return test_url.scheme in ("http", "https") and ref_url.netloc == test_url.netloc
