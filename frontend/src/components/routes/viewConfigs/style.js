import {makeStyles} from '@material-ui/core/index'

const useStyles = makeStyles(theme => ({
    table: {
        minWidth: 650,
    },
    snackbarIcon: {
        fontSize: 20,
        color: theme.palette.primary.main
    },
    snackbarIconVariant: {
        opacity: 0.9,
        marginRight: theme.spacing(1),
    },
    newConfigSnackbar: {
        backgroundColor: theme.palette.secondary.main
    },
    newConfigSnackbarMessage: {
        display: 'flex',
        alignItems: 'center',
        color: theme.palette.primary.main
    },
    typography: {
        padding: theme.spacing(2),
    },
}))

export default useStyles