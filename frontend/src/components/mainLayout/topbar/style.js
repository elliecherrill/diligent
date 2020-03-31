import {
    makeStyles
} from '@material-ui/core/index'
import React from 'react'

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
    }
}))
