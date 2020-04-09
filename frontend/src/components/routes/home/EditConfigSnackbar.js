import React, {useState} from 'react'
import useStyles from './style'
import {Snackbar, Button, IconButton, SnackbarContent} from '@material-ui/core'
import clsx from 'clsx'
import {
    Close as CloseIcon,
    Edit as EditIcon,
} from '@material-ui/icons'


const EditConfigSnackbar = ({title, setGoToViewsConfigs}) => {
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
                aria-describedby='client-snackbar'
                message={
                    <span id='client-snackbar' className={classes.newConfigSnackbarMessage}>
                    <EditIcon className={clsx(classes.snackbarIcon, classes.snackbarIconVariant)}/>
                    Edited Configuration: {title}
                </span>
                }
                action={[
                    <Button key='button' color='primary' size='small' onClick={() => {
                        handleClose()
                        setGoToViewsConfigs(true)
                    }}>
                        VIEW
                    </Button>,
                    <IconButton key='close' aria-label='close' color='inherit' onClick={handleClose}>
                        <CloseIcon className={classes.snackbarIcon}/>
                    </IconButton>
                ]}
            />
        </Snackbar>
    )
}

export default EditConfigSnackbar
