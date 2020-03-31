class Style:
    def __init__(self, bg, highlight):
        self.bg = bg
        self.highlight = highlight


WARNING = Style(bg="pale-yellow", highlight="amber")
ERROR = Style(bg="pale-red", highlight="red")
INFO = Style(bg="pale-blue", highlight="indigo")
SUCCESS = Style(bg="pale-green", highlight="green")