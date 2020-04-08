import React from 'react'
import {
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    Slide
} from '@material-ui/core'

const Transition = React.forwardRef(function Transition(props, ref) {
    return <Slide direction="up" ref={ref} {...props} />
})

const Alert = ({title, content, actions, open}) => {
    return <Dialog
        open={open}
        TransitionComponent={Transition}
        keepMounted
    >
        <DialogTitle id='alert-dialog-title'>{title}</DialogTitle>
        <DialogContent>
            <DialogContentText id='alert-dialog-description'>
                {content}
            </DialogContentText>
        </DialogContent>
        <DialogActions>
            {actions.map((action, index) => (<Button key={index} onClick={action.action}> {action.title} </Button>))}
        </DialogActions>
    </Dialog>
}

export default Alert