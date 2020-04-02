import {makeStyles} from '@material-ui/core/index'

const useStyles = makeStyles(theme => ({
    gridContainer: {
        padding: theme.spacing(2)
    },
    root: {
        display: 'flex',
        flexWrap: 'wrap',
        justifyContent: 'space-around',
        overflow: 'hidden',
        backgroundColor: theme.palette.background.paper,
        padding: theme.spacing(10)
    },
    whiteButton: {
        borderColor: 'white'
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
    }

}))

export default useStyles
