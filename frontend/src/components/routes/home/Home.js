import {
    Grid
} from '@material-ui/core/index'
import React from 'react'
import useStyles from './style'

const Home = props => {
    const classes = useStyles()

    // const [loaded, setLoaded] = useState(true)

    // const isSmallScreen = IsSmallScreen()

    // if (!loaded) {
    //     return (<div className={classes.root}>
    //         <Grid container justify='center' className={classes.gridContainer}>
    //             <Grid item>
    //                 <CircularProgress/>
    //             </Grid>
    //         </Grid>
    //     </div>)
    // }

    return (
        <div>
            <Grid container justify='center' className={classes.gridContainer}>
                <p>  Home Page </p>
            </Grid>
        </div>
    )
}

export default Home
