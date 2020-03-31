import {
    Grid,
    CircularProgress
} from '@material-ui/core/index'
import React, {useState} from 'react'
import useStyles from './style'
import {IsSmallScreen} from '../../../utils/responsiveUtils'

const Home = props => {
    const classes = useStyles()

    const [loaded, setLoaded] = useState(true)

    const isSmallScreen = IsSmallScreen()

    if (!loaded) {
        return (<div className={classes.root}>
            <Grid container justify='center' className={classes.gridContainer}>
                <Grid item>
                    <CircularProgress/>
                </Grid>
            </Grid>
        </div>)
    }

    return (
        <div>
            <Grid container justify='center' className={classes.gridContainer}>
                Home Page
            </Grid>
        </div>
    )
}

export default Home
