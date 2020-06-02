from flask import Blueprint, send_file

bp = Blueprint("plugin", __name__, url_prefix="/api/plugin")


@bp.route("/download", methods=["GET"])
def download_plugin():
    try:
        return send_file('plugin/diligent-1.0.0.zip', mimetype='application/zip',
                         as_attachment=True,
                         attachment_filename='diligent-1.0.0.zip')
    except Exception as e:
        return str(e)

@bp.route("/download_python", methods=["GET"])
def download_python_plugin():
    try:
        return send_file('plugin/diligent-for-python-1.0.0.zip', mimetype='application/zip',
                         as_attachment=True,
                         attachment_filename='diligent-for-python-1.0.0.zip')
    except Exception as e:
        return str(e)
