from markupsafe import Markup

from .styles import ERROR, INFO


class Message:
    def __init__(self, style, header="", body=""):
        self.header = Markup(header)
        self.body = Markup(body)
        self.style = style


class MessageEncoder:
    """
    Encode `Message` as a primitive dictionary
    to be passed to and read from views.
    """

    @staticmethod
    def encode(msg):
        return {"header": msg.header, "body": msg.body, "style": msg.style.__dict__}


###############################################################
#  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  | #
###############################################################


def _login_manager_message():
    return MessageEncoder.encode(
        Message(
            header="<h3><strong>Login Required.</strong></h3>",
            body="<p>You need to be logged in to access this page.</p>",
            style=INFO,
        )
    )


def _login_unsuccessful_error():
    return MessageEncoder.encode(
        Message(
            header="<h3><strong>Login failed.</strong></h3>",
            body="<p>You have attempted access with an invalid combination of login and password.</p>",
            style=ERROR,
        )
    )


#########################################################################
# Prepared messages, already serialised to dictionary for use
###########################################################################################

LOGIN_UNSUCCESSFUL_ERROR = _login_unsuccessful_error()
LOGIN_MANAGER_MESSAGE = _login_manager_message()
