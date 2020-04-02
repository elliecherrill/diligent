import authConstants from './constants/auth'
import axios from 'axios'

/**
 *
 * REST API
 *
 */
function getHeaders() {
    return {
        'Authorization': authConstants.ACCESS_TOKEN_HEADER()
    }
}

export function create_new_config(title, highPriorityChecks, mediumPriorityChecks, lowPriorityChecks) {
    let config = {
        'title': title,
        'high': highPriorityChecks.map(check => formatCheck(check)),
        'medium': mediumPriorityChecks.map(check => formatCheck(check)),
        'low': lowPriorityChecks.map(check => formatCheck(check))
    }

    console.log("Trying to post", config)
    return axios.post('/api/configuration/new_config', config, {headers: getHeaders()})
}

function formatCheck(check) {
    return {
        'check': check
    }
}