import React from 'react'
import {
    AppBar,
    IconButton,
    Toolbar,
    Typography,
    useScrollTrigger
} from '@material-ui/core/index'
import { Redirect } from 'react-router-dom'
import {
    ExitToApp as ExitIcon,
    Home as HomeIcon
} from '@material-ui/icons/index'
import { useStyles } from './style'
import routes from '../../../constants/routes'
import Tooltip from '@material-ui/core/Tooltip'
import logo from '../../../images/diligent-white.svg'

function ElevationScroll({ children, window }) {
    return React.cloneElement(children, {
        elevation: useScrollTrigger({ disableHysteresis: true, threshold: 0 })
            ? 4
            : 0
    })
}

export default props => {
    const { onLogoutAction } = props
    const classes = useStyles()

    return (
        <div>
            <ElevationScroll {...props}>
                <AppBar className={props.newWindow ? '' : classes.appBar} >
                    <Toolbar className={classes.toolbar}>
                        <img src={logo} alt='Diligent Logo' className={classes.logo}/>
                        <Typography variant='h6' className={classes.lastItemLeft}>
                            Diligent
                        </Typography>
                        <Tooltip title='Home'>
                            <IconButton
                                edge='end'
                                aria-label='Go To Home'
                                color='inherit'
                                onClick={() => props.setGoToHome(true)}>
                                <HomeIcon />
                            </IconButton>
                        </Tooltip>
                        <Tooltip title='Log out'>
                            <IconButton
                                edge='end'
                                aria-label='Log out'
                                color='inherit'
                                onClick={onLogoutAction}>
                                <ExitIcon />
                            </IconButton>
                        </Tooltip>
                    </Toolbar>
                </AppBar>

            </ElevationScroll>

            {props.goToHome && <Redirect to={routes.HOME}/>}

        </div>
    )
}
