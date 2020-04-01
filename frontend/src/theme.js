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
            light: '#f5f5f5',
            dark: '#f5f5f5',
            // main: '#efc050',
            main: '#ffffff',
            contrastText: '#000000'
        }
    },

})

theme = responsiveFontSizes(theme)

export default theme