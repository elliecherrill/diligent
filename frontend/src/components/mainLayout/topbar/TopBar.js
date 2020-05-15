import React, {useState} from 'react'
import {
    AppBar,
    IconButton,
    Toolbar,
    Typography,
    useScrollTrigger
} from '@material-ui/core/index'
import {Redirect} from 'react-router-dom'
import {
    ExitToApp as ExitIcon,
    HomeRounded as HomeIcon
} from '@material-ui/icons/index'
import {useStyles} from './style'
import routes from '../../../constants/routes'
import Tooltip from '@material-ui/core/Tooltip'
import logo from '../../../images/diligent-white.svg'
import python from '../../../images/python_icon.png'
import clsx from 'clsx'

function ElevationScroll({children, window}) {
    return React.cloneElement(children, {
        elevation: useScrollTrigger({disableHysteresis: true, threshold: 0})
            ? 4
            : 0
    })
}

export default props => {
    const {onLogoutAction} = props
    const classes = useStyles()

    const [expanded, setExpanded] = useState(false)

    return (
        <div>
            <ElevationScroll {...props}>
                <AppBar className={props.newWindow ? '' : classes.appBar}>
                    <Toolbar className={classes.toolbar}>
                        <img src={logo} alt='Diligent Logo' className={classes.logo}/>
                        <div className={classes.lastItemLeft} onClick={() => props.setGoToHome(true)}
                             style={{'cursor': 'pointer'}}>
                            <Typography variant='h6'>
                                Diligent
                            </Typography>
                        </div>
                        <Tooltip title='Diligent for Python'>
                            <IconButton
                                edge='end'
                                aria-label='Diligent for Python'
                                color='inherit'
                                onClick={() => {
                                    setExpanded(!expanded)
                                    props.setGoToPython(true)
                                }}>
                                <img
                                    src={python}
                                    alt='Diligent for Python'
                                    className={clsx(classes.expand, {
                                        [classes.expandOpen]: expanded,
                                    })}/>
                            </IconButton>
                        </Tooltip>
                        <Tooltip title='Home'>
                            <IconButton
                                edge='end'
                                aria-label='Go To Home'
                                color='inherit'
                                onClick={() => props.setGoToHome(true)}>
                                <HomeIcon/>
                            </IconButton>
                        </Tooltip>
                        <Tooltip title='Log out'>
                            <IconButton
                                edge='end'
                                aria-label='Log out'
                                color='inherit'
                                onClick={onLogoutAction}>
                                <ExitIcon/>
                            </IconButton>
                        </Tooltip>
                    </Toolbar>
                </AppBar>

            </ElevationScroll>

            {props.goToHome && <Redirect push to={routes.HOME}/>}
            {props.goToPython && <Redirect push to={routes.PYTHON}/>}

        </div>
    )
}
