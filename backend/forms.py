from wtforms import PasswordField, Form, StringField
from wtforms.validators import InputRequired


class LoginForm(Form):
    username = StringField("Username", [InputRequired()])
    password = PasswordField("Password", [InputRequired()])