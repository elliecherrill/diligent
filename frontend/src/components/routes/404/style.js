import {makeStyles} from '@material-ui/core/index'

const useStyles = makeStyles(theme => ({
    gridContainer: {
        padding: theme.spacing(2)
    },
    root: {
        display: 'flex',
        flexWrap: 'wrap',
        justifyContent: 'space-around',
        overflow: 'hidden',
        backgroundColor: theme.palette.background.paper,
        padding: theme.spacing(2),
    },
    img: {
        width: '50%',
        height: 'auto'
    }
}))

export default useStyles