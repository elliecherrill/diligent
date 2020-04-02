import React, {useState} from 'react'
import useStyles from './style'
import SnackbarContent from '@material-ui/core/SnackbarContent'
import IconButton from '@material-ui/core/IconButton'
import Snackbar from '@material-ui/core/Snackbar'
import clsx from 'clsx'

import {
    Close as CloseIcon,
    PostAdd as NewIcon,
} from '@material-ui/icons'


const NewConfigSnackbar = ({title}) => {
    const classes = useStyles()
    const [open, setOpen] = useState(true)

    const handleClose = () => {
        setOpen(false)
    }

    return (
        <Snackbar
            anchorOrigin={{
                vertical: 'bottom',
                horizontal: 'left',
            }}
            open={open}
            autoHideDuration={6000}>
            <SnackbarContent
                className={classes.newConfigSnackbar}
                aria-describedby="client-snackbar"
                message={
                    <span id="client-snackbar" className={classes.newConfigSnackbarMessage}>
                    <NewIcon className={clsx(classes.snackbarIcon, classes.snackbarIconVariant)}/>
                    New Configuration: {title}
                </span>
                }
                action={[
                    <IconButton key="close" aria-label="close" color="inherit" onClick={handleClose}>
                        <CloseIcon className={classes.snackbarIcon}/>
                    </IconButton>,
                ]}
            />
        </Snackbar>
    )
}

export default NewConfigSnackbar
