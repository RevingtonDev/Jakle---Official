import React, { Component } from "react";

export class Error extends Component {
    render() {
        return (
            <>
                <div className="layout-page page-container center row-flex">
                    <div className="error-code">404</div>
                    <p className="error-des">Page Not Found</p>
                </div>
            </>
        );
    }
}

