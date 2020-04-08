import React, {useState} from 'react'
import useStyles from './style'
import {Snackbar, IconButton, SnackbarContent} from '@material-ui/core'
import clsx from 'clsx'
import {
    Close as CloseIcon,
    DeleteOutline as DeleteIcon,
} from '@material-ui/icons'


const DeleteConfigSnackbar = () => {
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
                    <DeleteIcon className={clsx(classes.snackbarIcon, classes.snackbarIconVariant)}/>
                    Deleted Configuration
                </span>
                }
                action={[
                    <IconButton key="close" aria-label="close" color="inherit" onClick={handleClose}>
                        <CloseIcon className={classes.snackbarIcon}/>
                    </IconButton>
                ]}
            />
        </Snackbar>
    )
}

export default DeleteConfigSnackbar