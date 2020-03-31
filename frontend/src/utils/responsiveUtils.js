import useMediaQuery from '@material-ui/core/useMediaQuery'

/**
 * @return {boolean}
 */
export function IsVerySmallScreen() {
    return useMediaQuery('(max-width:400px)')
}

/**
 * @return {boolean}
 */
export function IsSmallScreen() {
    return useMediaQuery('(max-width:600px)')
}

/**
 * @return {boolean}
 */
export function IsMediumScreen() {
    return useMediaQuery('(max-width:930px)')
}

export function getDiagramCanvasDimensions() {
    const verySmallDimensions = [200, 225]
    const smallDimensions = [300, 225]
    const mediumDimensions = [500, 450]
    const bigDimensions = [500, 450]

    const verySmallScreen = IsVerySmallScreen()
    const smallScreen = IsSmallScreen()
    const mediumScreen = IsMediumScreen()

    if (verySmallScreen) {
        return verySmallDimensions
    } else if (smallScreen) {
        return smallDimensions
    } else if (mediumScreen) {
        return mediumDimensions
    } else {
        return bigDimensions
    }
}