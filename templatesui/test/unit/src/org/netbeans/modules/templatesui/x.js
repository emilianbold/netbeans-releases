print('Hello from ' + wizard.get('message'));
if (wizard.get('current')) {
    throw 'current should not be defined: ' + wizard.get('current');
}
if (wizard.get('errorCode')) {
    throw 'errorCode should not be defined: ' + wizard.get('errorCode');
}
if (wizard.get('steps')) {
    throw 'steps should not be defined: ' + wizard.get('steps');
}
