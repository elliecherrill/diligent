import {
    makeStyles
} from '@material-ui/core/index'

export const useStyles = makeStyles(theme => ({
    drawerButton: {
        marginRight: theme.spacing(2),
        [theme.breakpoints.up('md')]: {
            display: 'none'
        }
    },
    appBar: {
        // [theme.breakpoints.up('md')]: {
        //   marginLeft: drawerWidth,
        //   width: `calc(100% - ${drawerWidth}px)`
        // }
    },
    lastItemLeft: {
        flexGrow: 1
    },
    logo: {
        height: '24px',
        width: '24px',
        marginRight: theme.spacing(2)
    }
}))
