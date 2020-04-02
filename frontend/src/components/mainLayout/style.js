import { makeStyles } from '@material-ui/core'

const useStyles = makeStyles(theme => ({
    main: {
        flexGrow: 1,
        paddingTop: theme.spacing(3),
        paddingBottom: theme.spacing(3),
        paddingLeft: 0,
        paddingRight: 0
    },
    container: {
        marginTop: theme.spacing(8),
        paddingLeft: 0,
        paddingRight: 0
    }
}))

export default useStyles