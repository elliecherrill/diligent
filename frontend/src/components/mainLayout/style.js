import { makeStyles } from '@material-ui/core'

const useStyles = makeStyles(theme => ({
    main: {
        flexGrow: 1,
        paddingTop: theme.spacing(3),
        paddingBottom: theme.spacing(3),
    },
    container: {
        marginTop: theme.spacing(8),
        padding: theme.spacing(4)
    }
}))

export default useStyles