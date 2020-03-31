import { Grid, Typography } from '@material-ui/core/index'
import React from 'react'
import useStyles from './style'

const PageNotFound = props => {
    const classes = useStyles()

    return (
        <div className={classes.root}>
            {/*<Grid container justify='center' className={classes.gridContainer}>*/}
            {/*    <img src={error404} height={400} alt={'404 error'} className={classes.img}/>*/}
            {/*</Grid>*/}
            <Grid container justify='center' className={classes.gridContainer}>
                <Typography variant='h5' align='center'> Page Not Found </Typography>
            </Grid>
        </div>
    )
}

export default PageNotFound
