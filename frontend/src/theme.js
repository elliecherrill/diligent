import { createMuiTheme, responsiveFontSizes } from '@material-ui/core/styles'

let theme =  createMuiTheme({
    palette: {
        primary: {
            light: '#6581bb',
            dark: '#002d5d',
            main: '#34558b',
            contrastText: '#ffffff'
        },
        secondary: {
            light: '#fff280',
            dark: '#b9901d',
            main: '#efc050',
            contrastText: '#000000'
        }
    },

})

theme = responsiveFontSizes(theme)

export default theme