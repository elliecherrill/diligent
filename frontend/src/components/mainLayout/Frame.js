import React, {useState} from 'react'
import TopBar from './topbar/TopBar.js'
import { Container, CssBaseline } from '@material-ui/core/index'
import authenticationService from '../../utils/authenticationService'
import routes from '../../constants/routes'
import { BrowserRouter, Route, Switch } from 'react-router-dom'
import Home from '../routes/home/Home'
import NewConfig from '../routes/newConfig/NewConfig'
import useUserInformation from './topbar/userInfoHook'
import useStyles from './style'
import PageNotFound from '../routes/404/PageNotFound'
import ViewConfigs from '../routes/viewConfigs/ViewConfigs'
import EditConfig from '../routes/editConfig/EditConfig'
import Python from '../routes/python/Python'

const Frame = props => {

    const [goToHome, setGoToHome] = useState(false)
    const [goToPython, setGoToPython] = useState(false)
    const userInfo = useUserInformation(props.history)
    const classes = useStyles()

    const logout = event => {
        event.preventDefault()
        authenticationService.logout()
        props.history.push(routes.LOGIN)
    }

    return (
        <BrowserRouter>
            <div>
                <CssBaseline />
                <TopBar
                    onLogoutAction={logout}
                    goToHome={goToHome}
                    setGoToHome={setGoToHome}
                    userInfo={userInfo}
                    setGoToPython={setGoToPython}
                    goToPython={goToPython}
                />
                <div className={classes.main}>
                    <Container className={classes.container} maxWidth={false}>
                        <Switch>
                            <Route
                                exact
                                path={routes.HOME}
                                render={routeProps => {
                                    window.scrollTo(0, 0)
                                    return <Home userInfo={userInfo} setGoToHome={setGoToHome} {...routeProps} />
                                }}
                            />
                            <Route
                                exact
                                path={routes.PYTHON}
                                render={() => {
                                    window.scrollTo(0, 0)
                                    return <Python setGoToPython={setGoToPython} />
                                }}
                            />
                            <Route
                                exact
                                path={routes.NEW_CONFIG}
                                render={() => {
                                    window.scrollTo(0, 0)
                                    return <NewConfig />
                                }}
                            />
                            <Route
                                exact
                                path={routes.VIEW_CONFIGS}
                                render={() => {
                                    window.scrollTo(0, 0)
                                    return <ViewConfigs />
                                }}
                            />
                            <Route
                                exact
                                path={routes.EDIT_CONFIG + '/:id'}
                                render={() => {
                                    window.scrollTo(0, 0)
                                    return <EditConfig />
                                }}
                            />
                            <Route
                                render={() => {
                                    return <PageNotFound/>
                                }}
                            />
                        </Switch>
                    </Container>
                </div>
            </div>
        </BrowserRouter>
    )
}
export default Frame
