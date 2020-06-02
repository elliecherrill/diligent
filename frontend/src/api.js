import authConstants from './constants/auth'
import axios from 'axios'
import fileDownload from 'js-file-download'

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

export function create_new_config(title, highPriorityChecks, mediumPriorityChecks, lowPriorityChecks, courseCode, exerciseNum, type) {
    let config = {
        'title': title,
        'high': highPriorityChecks.map(check => formatCheck(check)),
        'medium': mediumPriorityChecks.map(check => formatCheck(check)),
        'low': lowPriorityChecks.map(check => formatCheck(check)),
        'courseCode': courseCode,
        'exerciseNum': exerciseNum,
        'type': type
    }

    return axios.post('/api/configuration/new_config', config, {headers: getHeaders()})
}

function formatCheck(check) {
    return {
        'check': check
    }
}

export function get_my_configs(type) {
    return axios.get('/api/configuration/get_my_configs/' + type, {headers: getHeaders()}).then(resp => resp.data)
}

export function get_checks_for_download(config_id) {
    return axios.get('/api/configuration/get_checks_for_download/' + config_id, {headers: getHeaders()}).then(resp => resp.data)
}

export function get_python_checks_for_download(config_id) {
    return axios.get('/api/configuration/get_python_checks_for_download/' + config_id, {headers: getHeaders()}).then(resp => resp.data)
}

export function get_checks(config_id) {
    return axios.get('/api/configuration/get_checks/' + config_id, {headers: getHeaders()}).then(resp => resp.data)
}

export function get_plugin() {
    return axios({
        url: '/api/plugin/download',
        responseType: 'blob',
        headers: getHeaders(),
        method: 'GET'
    }).then(resp => {
        fileDownload(resp.data, 'diligent-1.0.0.zip')
        return true
    }).catch(() => false)
}

export function get_python_plugin() {
    return axios({
        url: '/api/plugin/download_python',
        responseType: 'blob',
        headers: getHeaders(),
        method: 'GET'
    }).then(resp => {
        fileDownload(resp.data, 'diligent-for-python-1.0.0.zip')
        return true
    }).catch(() => false)
}

export function delete_config(config_id) {
    return axios.delete('/api/configuration/' + config_id, {headers: getHeaders()}).then(resp => resp.data)
}
