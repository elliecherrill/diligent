import { makeStyles } from '@material-ui/core/index'

export default makeStyles(theme => ({
    '@global': {
        body: {
            backgroundColor: theme.palette.common.white
        }
    },
    paper: {
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        height: '100vh',
        paddingBottom: theme.spacing(10)
    },
    avatar: {
        margin: theme.spacing(1),
        backgroundColor: theme.palette.secondary.main
    },
    form: {
        width: '80%',
        marginRight: '10%',
        marginLeft: '10%'
    },
    submit: {
        margin: theme.spacing(3, 0, 2)
    },
    loginContainer: {
        display: 'flex',
        flexDirection: 'row',
        alignContent: 'center'
    }
}))